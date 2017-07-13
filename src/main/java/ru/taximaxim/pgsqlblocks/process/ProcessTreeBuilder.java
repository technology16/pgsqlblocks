/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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
package ru.taximaxim.pgsqlblocks.process;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.SortColumn;
import ru.taximaxim.pgsqlblocks.SortDirection;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcStatus;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessTreeBuilder {

    private static final Logger LOG = Logger.getLogger(ProcessTreeBuilder.class);

    private static final String PID = "pid";
    private static final String DATNAME = "datname";
    private static final String USENAME = "usename";
    private static final String CLIENT = "client";
    private static final String STATE = "state";
    private static final String BLOCKEDBY = "blockedBy";
    private static final String RELATION = "relation";
    private static final String LOCKTYPE = "locktype";
    private static final String SLOWQUERY = "slowQuery";
    private static final String APPLICATIONNAME = "application_name";
    private static final String BACKENDSTART = "backend_start";
    private static final String QUERYSTART = "query_start";
    private static final String XACTSTART = "xact_start";
    private static final String STATECHANGE = "state_change";
    private static final String QUERYSQL = "query";
    private static final String GRANTED = "granted";

    private static final String QUERYFILENAME = "query.sql";
    private static final String QUERYWITHIDLEFILENAME = "query_with_idle.sql";

    private static final String GRANTED_FLAG = "t";

    private Settings settings = Settings.getInstance();
    private static String queryWithoutIdle;
    private static String queryWithIdle;
    private final DbcData dbcData;
    private final Process root = new Process(0, null, null, null, null);

    public ProcessTreeBuilder(DbcData dbcData) {
        this.dbcData = dbcData;
        queryWithIdle = loadQuery(QUERYWITHIDLEFILENAME);
        queryWithoutIdle = loadQuery(QUERYFILENAME);
    }

    public Process buildProcessTree() {
        root.clearChildren();
        queryProcessTree().stream()
            .filter(process -> !process.hasParent())
            .forEach(root::addChildren);
        return root;
    }

    private Collection<Process> queryProcessTree() {
        Map<Integer, Process> tempProcessList = new HashMap<>();
        Map<Integer, Set<Block>> tempBlocksList = new HashMap<>();
        if (dbcData.getConnection() == null) {
            return tempProcessList.values();
        }
        try {
            if (dbcData.getConnection().isClosed()) {
                dbcData.connect();
            }
        } catch (SQLException e) {
            LOG.error(String.format("Ошибка при переподключении к %s", dbcData.getName()), e);
        }
        try (
                // TODO do not prepare each time
                PreparedStatement statement = dbcData.getConnection().prepareStatement(getQuery(settings.getShowIdle()));
                ResultSet processSet = statement.executeQuery()
        ) {
            int backendPid = dbcData.getBackendPid();
            while (processSet.next()) {
                int pid = processSet.getInt(PID);
                if (backendPid == pid && !settings.getShowBackendPid()) {
                    continue;
                }
                Process currentProcess = tempProcessList.get(pid);
                if (currentProcess == null) {
                    currentProcess = createProcessFromResultSet(processSet);
                    tempProcessList.put(pid, currentProcess);
                }

                int blockedBy = processSet.getInt(BLOCKEDBY);
                if (blockedBy != 0) {
                    Block block = new Block(blockedBy,
                                            processSet.getString(LOCKTYPE),
                                            processSet.getString(RELATION),
                                            GRANTED_FLAG.equals(processSet.getString(GRANTED)));
                    tempBlocksList.computeIfAbsent(currentProcess.getPid(), k -> new HashSet<>()).add(block);
                }
            }
        } catch (SQLException e) {
            LOG.error(String.format("Ошибка при получении процессов для %s", dbcData.getDbname()), e);
        }

        proceedBlocks(tempProcessList, tempBlocksList);

        proceedProcesses(tempProcessList);

        return tempProcessList.values();
    }

    private Process createProcessFromResultSet(ResultSet resultSet) throws SQLException {
        int pid = resultSet.getInt(PID);
        String state = resultSet.getString(STATE);
        String stateChangeDate = dateParse(resultSet.getString(STATECHANGE));
        Query query = new Query(resultSet.getString(QUERYSQL),
                dateParse(resultSet.getString(BACKENDSTART)),
                dateParse(resultSet.getString(QUERYSTART)),
                dateParse(resultSet.getString(XACTSTART)),
                resultSet.getBoolean(SLOWQUERY));
        QueryCaller caller = new QueryCaller(resultSet.getString(APPLICATIONNAME),
                resultSet.getString(DATNAME),
                resultSet.getString(USENAME),
                resultSet.getString(CLIENT));
        return new Process(pid, caller, query, state, stateChangeDate);
    }

    private void proceedBlocks(Map<Integer, Process> tempProcessList, Map<Integer, Set<Block>> tempBlocksList) {
        for (Map.Entry<Integer, Set<Block>> entry : tempBlocksList.entrySet()) {
            int blockedPid = entry.getKey();
            Set<Block> blocks = entry.getValue();
            blocks.stream()
                    .filter(Block::isGranted)
                    .forEach(block -> {
                        int blockingPid = block.getBlockingPid();
                        if (tempBlocksList.containsKey(blockingPid)) {
                            Set<Block> blockingIsBlockedBy = tempBlocksList.get(blockingPid);
                            Optional<Block> cycleBlock = blockingIsBlockedBy.stream()
                                    .filter(b -> b.getBlockingPid() == blockedPid)
                                    .filter(b -> b.getRelation().equals(block.getRelation()))
                                    .filter(b -> b.getLocktype().equals(block.getLocktype()))
                                    .filter(b -> b.isGranted())
                                    .findFirst();
                            if (cycleBlock.isPresent()) {
                                proceedBlocksWithCycle(tempProcessList, blockedPid, blockingPid, block, cycleBlock.get());
                            } else {
                                tempProcessList.get(blockedPid).addBlock(block);
                            }
                        } else {
                            tempProcessList.get(blockedPid).addBlock(block);
                        }
                    });
        }
    }

    private void proceedBlocksWithCycle(Map<Integer, Process> tempProcessList,
                                        int blockedPid, int blockingPid, Block b, Block reversedBlock) {
        if (b.isGranted()) {
            Process blockedProcess = getProcessByPid(tempProcessList.values(), blockedPid);
            Process blockingProcess = getProcessByPid(tempProcessList.values(), blockingPid);

            // TODO Do not truncate timestamps to seconds
            // TODO compare date objects instead of simple String.compareTo
            // then latest started process get Block
            if ((blockingProcess.getQuery().getQueryStart()).compareTo(blockedProcess.getQuery().getQueryStart()) <= 0) {
                blockedProcess.addBlock(new Block(blockingPid, b.getLocktype(), b.getRelation(), b.isGranted()));
            } else {
                blockingProcess.addBlock(new Block(blockedPid, b.getLocktype(), b.getRelation(), b.isGranted()));
            }
        } else {
            tempProcessList.get(blockingPid).addBlock(reversedBlock);
        }
    }

    private void proceedProcesses(Map<Integer, Process> tempProcessList) {
        dbcData.setStatus(DbcStatus.CONNECTED);
        dbcData.setContainBlockedProcess(false);
        for (Process process : tempProcessList.values()) {
            process.getBlocks().forEach(blockedBy -> {
                //Добавляем для данного процесса родителя с pid = process.getBlocks()
                Process parentProcess = getProcessByPid(tempProcessList.values(), blockedBy.getBlockingPid());
                if (parentProcess != null) {
                    process.setParents(parentProcess);
                    parentProcess.addChildren(process);
                    parentProcess.setStatus(ProcessStatus.BLOCKING);
                    process.setStatus(ProcessStatus.BLOCKED);
                    dbcData.setContainBlockedProcess(true);
                }
            });
        }
    }

    private Process getProcessByPid(Collection<Process> processList, int pid) {
        return processList.stream().filter(process -> process.getPid() == pid).findFirst().orElse(null);
    }

    private int stringCompare(String s1, String s2, SortDirection sortDirection) {
        return sortDirection == SortDirection.DOWN ? s1.compareTo(s2) : s2.compareTo(s1);
    }
    
    public void processSort(Process rootProcess, SortColumn sortColumn, SortDirection sortDirection) {
        rootProcess.getChildren().sort((Process process1, Process process2) ->
                getSortDirectionByColumn(sortColumn, sortDirection, process1, process2));
    }

    /* TODO: need to refactor LOCKTYPE and RELATION cases
    For example, by CollectionUtils.isEqualCollection(java.util.Collection a, java.util.Collection b) maybe */
    private int getSortDirectionByColumn(SortColumn sortColumn, SortDirection sortDirection,
                                           Process process1, Process process2) {
        switch (sortColumn) {
            case PID:
                if (process1.getPid() > process2.getPid()) {
                    return sortDirection == SortDirection.UP ? -1 : 1;
                } else if (process1.getPid() < process2.getPid()) {
                    return sortDirection == SortDirection.UP ? 1 : -1;
                } else {
                    return 0;
                }
            case BLOCKED_COUNT:
                if (process1.getChildrenCount() > process2.getChildrenCount()) {
                    return sortDirection == SortDirection.UP ? -1 : 1;
                } else if (process1.getChildrenCount() < process2.getChildrenCount()) {
                    return sortDirection == SortDirection.UP ? 1 : -1;
                } else {
                    return 0;
                }
            case APPLICATION_NAME:
                return stringCompare(process1.getCaller().getApplicationName(), process2.getCaller().getApplicationName(), sortDirection);
            case DATNAME:
                return stringCompare(process1.getCaller().getDatname(), process2.getCaller().getDatname(), sortDirection);
            case USENAME:
                return stringCompare(process1.getCaller().getUsername(), process2.getCaller().getUsername(), sortDirection);
            case CLIENT:
                return stringCompare(process1.getCaller().getClient(), process2.getCaller().getClient(), sortDirection);
            case BACKEND_START:
                return stringCompare(process1.getQuery().getBackendStart(), process2.getQuery().getBackendStart(), sortDirection);
            case QUERY_START:
                return stringCompare(process1.getQuery().getQueryStart(), process2.getQuery().getQueryStart(), sortDirection);
            case XACT_START:
                return stringCompare(process1.getQuery().getXactStart(), process2.getQuery().getXactStart(), sortDirection);
            case STATE:
                return stringCompare(process1.getState(), process2.getState(), sortDirection);
            case STATE_CHANGE:
                return stringCompare(process1.getStateChange(), process2.getStateChange(), sortDirection);
            case QUERY:
                return stringCompare(process1.getQuery().getQueryString(), process2.getQuery().getQueryString(), sortDirection);
            case SLOWQUERY:
                return Boolean.compare(process1.getQuery().isSlowQuery(), process2.getQuery().isSlowQuery())
                        * (sortDirection == SortDirection.UP ? 1 : -1);
            case BLOCKED:
                return stringCompare(process1.getBlocks().toString(), process2.getBlocks().toString(), sortDirection);
            case LOCKTYPE:
                return stringCompare(
                        process1.getBlocks().stream().map(Block::getLocktype).sorted().collect(Collectors.joining(", ")),
                        process2.getBlocks().stream().map(Block::getLocktype).sorted().collect(Collectors.joining(", ")),
                        sortDirection);
            case RELATION:
                return stringCompare(
                    process1.getBlocks().stream().map(Block::getRelation).sorted().collect(Collectors.joining(", ")),
                    process2.getBlocks().stream().map(Block::getRelation).sorted().collect(Collectors.joining(", ")),
                    sortDirection);
            default:
                return 0;
        }
    }

    private String dateParse(String dateString) {
        if(dateString == null || dateString.length() == 0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX");
        SimpleDateFormat sdfp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            LOG.error("Формат даты " + dateString + " не поддерживается", e);
        }
        return sdfp.format(date);
    }


    private String getQuery(boolean showIdle) {
        if (showIdle) {
            return queryWithIdle;
        } else {
            return queryWithoutIdle;
        }
    }

    private String loadQuery(String queryFile){
        try (InputStream input = ClassLoader.getSystemResourceAsStream(queryFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            return out.toString();
        } catch (IOException e) {
            LOG.error("Ошибка чтения файла " + queryFile, e);
            return null;
        }
    }
}
