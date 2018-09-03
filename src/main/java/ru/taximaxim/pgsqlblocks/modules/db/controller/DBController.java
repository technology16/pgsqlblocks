/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
 * %
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.modules.db.controller;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import ru.taximaxim.pgpass.PgPass;
import ru.taximaxim.pgpass.PgPassException;
import ru.taximaxim.pgsqlblocks.common.DBQueries;
import ru.taximaxim.pgsqlblocks.common.models.*;
import ru.taximaxim.pgsqlblocks.modules.db.model.DBStatus;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.Settings;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.taximaxim.pgsqlblocks.PgSqlBlocks.APP_NAME;

public class DBController implements DBProcessFilterListener, DBBlocksJournalListener {

    private DBModel model;

    private final List<DBProcess> processes = new ArrayList<>();
    private final List<DBProcess> filteredProcesses = new ArrayList<>();

    private static final Logger LOG = Logger.getLogger(DBController.class);

    private static final String PG_BACKEND_PID = "pg_backend_pid";
    private static final String BLOCKED_BY = "blockedBy";

    private List<DBControllerListener> listeners = new ArrayList<>();

    private final DBProcessFilter processesFilters = new DBProcessFilter();

    private final Settings settings;
    private final ResourceBundle resourceBundle;

    private DBStatus status = DBStatus.DISABLED;

    private boolean blocked = false;

    private int backendPid;

    private Connection connection;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService journalsSaveExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> updater;

    private final DBBlocksJournal blocksJournal = new DBBlocksJournal();

    private Date blocksJournalCreateDate;
    private final DateUtils dateUtils = new DateUtils();

    public DBController(Settings settings, DBModel model) {
        this.settings = settings;
        this.resourceBundle = settings.getResourceBundle();
        this.model = model;
        blocksJournalCreateDate = new Date();
        blocksJournal.addListener(this);
        processesFilters.addListener(this);
    }

    public DBModel getModel() {
        return model.clone();
    }

    public void setModel(DBModel model) {
        this.model = model;
    }

    private String getConnectionUrl() {
        return String.format("jdbc:postgresql://%1$s:%2$s/%3$s?ApplicationName=%4$s",
                                model.getHost(), model.getPort(), model.getDatabaseName(), APP_NAME);
    }

    public boolean isEnabledAutoConnection() {
        return model.isEnabled();
    }

    public synchronized void connect() {
        executor.execute(() -> {
            String password = getPassword();
            try {
                listeners.forEach(listener -> listener.dbControllerWillConnect(this));

                Properties info = new Properties();
                info.put("user", model.getUser());
                info.put("password", password);
                info.put("loginTimeout", String.valueOf(settings.getLoginTimeout()));

                connection = DriverManager.getConnection(getConnectionUrl(), info);
                setBackendPid(getPgBackendPid());
                setStatus(DBStatus.CONNECTED);
                listeners.forEach(listener -> listener.dbControllerDidConnect(this));
            } catch (SQLException e) {
                setStatus(DBStatus.CONNECTION_ERROR);
                listeners.forEach(listener -> listener.dbControllerConnectionFailed(this, e));
            }
        });
    }

    public String getPassword() {
        if (model.hasPassword()) {
            return model.getPassword();
        }
        String password = "";
        try {
            String pgPass = PgPass.get(model.getHost(), model.getPort(), model.getDatabaseName(), model.getUser());
            password = pgPass == null ? password : pgPass;
        } catch (PgPassException e) {
            LOG.error("Ошибка получения пароля из pgpass файла " + e.getMessage(), e);
        }
        return password;
    }

    public void disconnect(boolean forcedByUser) {
        setBackendPid(0);
        stopProcessesUpdater();
        try {
            connection.close();
            setStatus(forcedByUser ? DBStatus.DISABLED : DBStatus.CONNECTION_ERROR);
            listeners.forEach(listener -> listener.dbControllerDidDisconnect(this, forcedByUser));
        } catch (SQLException exception) {
            setStatus(DBStatus.CONNECTION_ERROR);
            listeners.forEach(listener -> listener.dbControllerDisconnectFailed(this, forcedByUser, exception));
        }
    }

    public DBProcessFilter getProcessesFilters() {
        return processesFilters;
    }

    private int getPgBackendPid() throws SQLException {
        try (Statement stBackendPid = connection.createStatement();
             ResultSet resultSet = stBackendPid.executeQuery(DBQueries.PG_BACKEND_PID_QUERY)) {
            if (resultSet.next()) {
                return resultSet.getInt(PG_BACKEND_PID);
            }
            return 0;
        }
    }

    public int getBackendPid() {
        return backendPid;
    }

    private void setBackendPid(int backendPid) {
        this.backendPid = backendPid;
    }

    private void setBlocked(boolean blocked) {
        if (this.blocked != blocked) {
            this.blocked = blocked;
            listeners.forEach(listener -> listener.dbControllerBlockedChanged(this));
        }
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isConnected() {
        try {
            return !(connection == null || connection.isClosed());
        } catch (SQLException e) {
            LOG.error(String.format(resourceBundle.getString("error_on_check_is_connected"), e.getMessage()));
            return false;
        }
    }

    public List<DBProcess> getProcesses() {
        return processes;
    }

    // TODO possible duplicate processes with same pid
    public int getProcessesCount() {
        return processes.size() + processes.stream().mapToInt(this::countChildren).sum();
    }

    private int countChildren(DBProcess process) {
        return process.getChildren().size() + process.getChildren().stream().mapToInt(this::countChildren).sum();
    }

    public List<DBProcess> getFilteredProcesses() {
        return filteredProcesses;
    }

    public DBBlocksJournal getBlocksJournal() {
        return blocksJournal;
    }

    public DBStatus getStatus() {
        return status;
    }

    private void setStatus(DBStatus status) {
        this.status = status;
        listeners.forEach(listener -> listener.dbControllerStatusChanged(this, this.status));
    }

    public void startProcessesUpdater(long initDelay) {
        stopProcessesUpdater();
        updater = executor.scheduleWithFixedDelay(this::updateProcesses,
                                                    initDelay, settings.getUpdatePeriod(), TimeUnit.SECONDS);
    }

    public void startProcessesUpdater() {
        startProcessesUpdater(0);
    }

    public void stopProcessesUpdater() {
        if (updater != null) {
            updater.cancel(true);
        }
    }

    public void updateProcesses() {
        if (!isConnected()) {
            // TODO: 03.09.18 tuta
            connect();
        } else {
            executor.execute(this::loadProcesses);
        }
    }

    public void shutdown() {
        stopProcessesUpdater();
        executor.shutdownNow();
        journalsSaveExecutor.shutdown();
        saveUnclosedBlockedProcessesToFile();
    }

    private void loadProcesses() {
        listeners.forEach(listener -> listener.dbControllerWillUpdateProcesses(this));
        try(
                PreparedStatement preparedStatement = connection.prepareStatement(getProcessesQuery());
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            Map<Integer, DBProcess> tmpProcesses = new HashMap<>();
            Map<Integer, Set<DBBlock>> tmpBlocks = new HashMap<>();
            DBProcessSerializer processDeserializer = new DBProcessSerializer();
            DBBlockDeserializer blockDeserializer = new DBBlockDeserializer();
            while(resultSet.next()) {
                DBProcess process = processDeserializer.deserialize(resultSet);
                int blockedBy = resultSet.getInt(BLOCKED_BY);
                if (blockedBy != 0) {
                    DBBlock block = blockDeserializer.deserialize(resultSet);
                    tmpBlocks.computeIfAbsent(process.getPid(), k -> new HashSet<>()).add(block);
                }
                tmpProcesses.put(process.getPid(), process);
            }
            proceedBlocks(tmpProcesses, tmpBlocks);
            proceedProcesses(tmpProcesses);
            processesLoaded(tmpProcesses.values().stream().filter(process -> !process.hasParent()).collect(Collectors.toList()));
        } catch (SQLException e) {
            LOG.error(String.format("Ошибка при получении процессов для %s", model.getName()), e);
            disconnect(false);
        }
    }

    private void proceedBlocks(Map<Integer, DBProcess> tmpProcesses, Map<Integer, Set<DBBlock>> tmpBlocks) {
        for (Map.Entry<Integer, Set<DBBlock>> entry : tmpBlocks.entrySet()) {
            int blockedPid = entry.getKey();
            Set<DBBlock> blocks = entry.getValue();
            blocks.stream()
                    .filter(DBBlock::isGranted)
                    .forEach(block -> {
                        int blockingPid = block.getBlockingPid();
                        if (tmpBlocks.containsKey(blockingPid)) {
                            Set<DBBlock> blockingIsBlockedBy = tmpBlocks.get(blockingPid);
                            Optional<DBBlock> cycleBlock = blockingIsBlockedBy.stream()
                                    .filter(b -> b.getBlockingPid() == blockedPid)
                                    .filter(b -> b.getRelation().equals(block.getRelation()))
                                    .filter(b -> b.getLocktype().equals(block.getLocktype()))
                                    .filter(DBBlock::isGranted)
                                    .findFirst();
                            if (cycleBlock.isPresent()) {
                                proceedBlocksWithCycle(tmpProcesses, blockedPid, blockingPid, block, cycleBlock.get());
                            } else {
                                tmpProcesses.get(blockedPid).addBlock(block);
                            }
                        } else {
                            tmpProcesses.get(blockedPid).addBlock(block);
                        }
                    });
        }
    }

    private void proceedBlocksWithCycle(Map<Integer, DBProcess> tmpProcesses,
                                        int blockedPid, int blockingPid, DBBlock b, DBBlock reversedBlock) {
        DBProcess blockedProcess = tmpProcesses.get(blockedPid);
        DBProcess blockingProcess = tmpProcesses.get(blockingPid);
        if (b.isGranted()) {
            if ((blockingProcess.getQuery().getQueryStart()).compareTo(blockedProcess.getQuery().getQueryStart()) <= 0) {
                blockedProcess.addBlock(new DBBlock(blockingPid, b.getRelation(), b.getLocktype(), b.isGranted()));
            } else {
                blockingProcess.addBlock(new DBBlock(blockedPid, b.getRelation(), b.getLocktype(), b.isGranted()));
            }
        } else {
            blockingProcess.addBlock(reversedBlock);
        }
    }

    private void proceedProcesses(Map<Integer, DBProcess> tmpProcesses) {
        for (DBProcess process : tmpProcesses.values()) {
            process.getBlocks().forEach(blockedBy -> {
                Optional<DBProcess> processOptional = tmpProcesses.values().stream().filter(p -> p.getPid() == blockedBy.getBlockingPid()).findFirst();
                if (processOptional.isPresent()) {
                    DBProcess parentProcess = processOptional.get();
                    process.addParent(parentProcess);
                    parentProcess.addChild(process);
                    parentProcess.setStatus(DBProcessStatus.BLOCKING);
                    process.setStatus(DBProcessStatus.BLOCKED);
                }
            });
        }
    }

    private void processesLoaded(List<DBProcess> loadedProcesses) {
        processes.clear();
        processes.addAll(loadedProcesses);

        filteredProcesses.clear();
        if (processesFilters.isEnabled()) {
            filteredProcesses.addAll(processes.stream().filter(processesFilters::filter).collect(Collectors.toList()));
        } else {
            filteredProcesses.addAll(processes);
        }

        blocksJournal.add(loadedProcesses.stream().filter(DBProcess::hasChildren).collect(Collectors.toList()));

        listeners.forEach(listener -> listener.dbControllerProcessesUpdated(this));
        boolean hasBlockedProcesses = processes.stream().anyMatch(DBProcess::hasChildren);
        setBlocked(hasBlockedProcesses);
    }

    private String getProcessesQuery() {
        if (settings.getShowIdle()) {
            return DBQueries.getProcessesQueryWithIdle();
        } else {
            return DBQueries.getProcessesQuery();
        }
    }

    Connection getConnection() {
        return connection;
    }

    public void addListener(DBControllerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBControllerListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBController)) return false;

        DBController that = (DBController) o;

        return model.equals(that.model);
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    @Override
    public void dbProcessFilterChanged() {
        filteredProcesses.clear();
        filteredProcesses.addAll(processes.stream().filter(processesFilters::filter).collect(Collectors.toList()));
        listeners.forEach(listener -> listener.dbControllerProcessesFilterChanged(this));
    }

    public boolean terminateProcessWithPid(int processPid) throws SQLException {
        boolean processTerminated = false;
        // FIXME prepare once
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBQueries.PG_TERMINATE_BACKEND_QUERY)) {
            preparedStatement.setInt(1, processPid);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                processTerminated = resultSet.getBoolean(1);
            }
        }
        return processTerminated;
    }

    public boolean cancelProcessWithPid(int processPid) throws SQLException {
        boolean processCanceled = false;
        // FIXME prepare once
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBQueries.PG_CANCEL_BACKEND_QUERY)) {
            preparedStatement.setInt(1, processPid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                processCanceled = resultSet.getBoolean(1);
            }
        }
        return processCanceled;
    }

    @Override
    public void dbBlocksJournalDidAddProcesses() {
        listeners.forEach(listener -> listener.dbControllerBlocksJournalChanged(this));
    }

    @Override
    public void dbBlocksJournalDidCloseAllProcesses() {
        listeners.forEach(listener -> listener.dbControllerBlocksJournalChanged(this));
    }

    @Override
    public void dbBlocksJournalDidCloseProcesses(List<DBBlocksJournalProcess> processes) {
        asyncSaveClosedBlockedProcessesToFile(processes);
    }

    @Override
    public void dbBlocksJournalDidChangeFilters() {
        listeners.forEach(listener -> listener.dbControllerBlocksJournalChanged(this));
    }

    private void asyncSaveClosedBlockedProcessesToFile(List<DBBlocksJournalProcess> processes) {
        journalsSaveExecutor.execute(() -> {
            try {
                saveBlockedProcessesToFile(processes);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                LOG.error("Error while saving blocked processes to journal", e);
            }
        });
    }

    private void saveBlockedProcessesToFile(List<DBBlocksJournalProcess> processes) throws ParserConfigurationException, IOException, SAXException {

        String fileName = String.format("%s-%s.xml", this.model.getName(), dateUtils.dateToString(blocksJournalCreateDate));
        Path blocksJournalsDirPath = PathBuilder.getInstance().getBlocksJournalsDir();
        Path currentJournalPath = Paths.get(blocksJournalsDirPath.toString(), fileName);
        File currentJournalFile = currentJournalPath.toFile();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        boolean fileExists = currentJournalFile.exists();
        Document document;
        Element rootElement;
        if (fileExists) {
            document = documentBuilder.parse(currentJournalFile);
            rootElement = document.getDocumentElement();
        } else {
            document = documentBuilder.newDocument();
            rootElement = document.createElement("blocksJournal");
        }
        DBBlocksJournalProcessSerializer serializer = new DBBlocksJournalProcessSerializer();
        processes.forEach(journalProcess -> {
            Element el = serializer.serialize(document, journalProcess);
            rootElement.appendChild(el);
        });
        if (!fileExists) {
            document.appendChild(rootElement);
        }
        XmlDocumentWorker documentWorker = new XmlDocumentWorker();
        documentWorker.save(document, currentJournalFile);
    }

    private void saveUnclosedBlockedProcessesToFile() {
        if (blocksJournal.isEmpty()) {
            return;
        }
        List<DBBlocksJournalProcess> openedBlockedProcesses = blocksJournal.getProcesses().stream().filter(DBBlocksJournalProcess::isOpened).collect(Collectors.toList());
        if (openedBlockedProcesses.isEmpty()) {
            return;
        }
        try {
            saveBlockedProcessesToFile(openedBlockedProcesses);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
