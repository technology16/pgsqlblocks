package ru.taximaxim.pgsqlblocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import ru.taximaxim.pgsqlblocks.ui.MainForm;

/**
 * Класс для работы с серверными процессами
 * 
 * @author ismagilov_mg
 */
public class Provider {
    
    protected static final Logger LOG = Logger.getLogger(Provider.class);
    
    private static final String PID = "pid";
    private static final String DATNAME = "datname";
    private static final String USENAME = "usename";
    private static final String CLIENT = "client";
    private static final String STATE = "state";
    private static final String BLOCKEDBY = "blockedBy";
    private static final String BLOCKING_LOCKS = "blocking_locks";
    private static final String SLOWQUERY = "slowQuery";
    private static final String APPLICATIONNAME = "application_name";
    private static final String BACKENDSTART = "backend_start";
    private static final String QUERYSTART = "query_start";
    private static final String XACTSTART = "xact_start";
    private static final String STATECHANGE = "state_change";
    private static final String QUERYSQL = "query";
    private static final int LOGINTIMEOUT = 5;
    
    private static String query;
    
    static {
        DriverManager.setLoginTimeout(LOGINTIMEOUT);
    }
    
    private DbcData dbcData;
    private Connection connection;
    private ConcurrentMap<Integer, Process> processMap;
    private List<Process> processList;
    private Runnable getProcesses;
    
    private String getQuery() throws IOException {
        if(query == null) {
            try (
                    BufferedReader reader = Files.newBufferedReader(
                            Paths.get("query.sql"), StandardCharsets.UTF_8);
                    ) {

                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                query = out.toString();
            }
        }
        return query;
    }
    
    public Provider(DbcData dbcData) {
        this.dbcData = dbcData;
    }
    
    private ConcurrentMap<Integer, Process> getProcessMap(){
        if(processMap == null) {
            processMap = new ConcurrentHashMap<Integer, Process>();
        }
        return processMap;
    }
    private void clearProcessMap(){
        processMap = new ConcurrentHashMap<Integer, Process>();
    }
    
    private Runnable connect = new Runnable(){
        @Override
        public void run() {
            connect();
        }
    };
    
    public Runnable getConnection() {
        synchronized (connect) {
            return connect;
        }
    }
    
    private void connect(){
        if(isConnected()) {
            LOG.info(getDbcData().getName() + " соединение уже создано");
            return;
        }
        try {
            LOG.info(getDbcData().getName() + " Соединение...");
            connection = DriverManager.getConnection(getDbcData().getUrl(), getDbcData().getUser(), getDbcData().getPasswd());
            getDbcData().setStatus(DbcStatus.CONNECTED);
            LOG.info(getDbcData().getName() + " Соединение создано.");
        } catch (SQLException e) {
            getDbcData().setStatus(DbcStatus.ERROR);
            LOG.error(getDbcData().getName() + " " + e.getMessage(), e);
        } finally {
            if(MainForm.getInstance().getShell() != null && !MainForm.getInstance().getShell().isDisposed()) {
                MainForm.getInstance().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        MainForm.getInstance().serverStatusUpdate(getDbcData());
                    }
                });
            }
        }
    }

    public void disconnect() {
        if(connection != null) {
            try {
                connection.close();
                connection = null;
                getDbcData().setStatus(DbcStatus.DISABLED);
                LOG.info(getDbcData().getName() + " Соединение закрыто.");
            } catch (SQLException e) {
                connection = null;
                getDbcData().setStatus(DbcStatus.ERROR);
                LOG.error(getDbcData().getName() + " " + e.getMessage(), e);
            } finally {
                if(MainForm.getInstance().getShell() != null && !MainForm.getInstance().getShell().isDisposed()){
                    MainForm.getInstance().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            MainForm.getInstance().serverStatusUpdate(getDbcData());
                        }
                    });
                }
            }
        }
    }
    
    public Runnable getProc() {
        if(getProcesses == null) {
            getProcesses = new Runnable() {
                @Override
                public void run() {
                    if(!isConnected()) {
                        return;
                    }
                    try {
                        getProcesses();
                        if(getProcessMap().size() > getProcessList().size()) {
                            getDbcData().setStatus(DbcStatus.BLOCKED);
                            for(Process process : getProcessList()) {
                                if(process.getChildren().size() > 0){
                                    BlocksHistory.getInstance().add(getDbcData(), process);
                                }
                            }
                        } else {
                            getDbcData().setStatus(DbcStatus.CONNECTED);
                        }
                    } catch (SQLException e) {
                        getDbcData().setStatus(DbcStatus.ERROR);
                        LOG.error(getDbcData().getName() + " " + e.getMessage(), e);
                    } catch (IOException e) {
                        getDbcData().setStatus(DbcStatus.ERROR);
                        LOG.error(getDbcData().getName() + " " + e.getMessage(), e);
                    } finally {
                        if(MainForm.getInstance().getShell() != null && !MainForm.getInstance().getShell().isDisposed()){
                            MainForm.getInstance().getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    MainForm.getInstance().serverStatusUpdate(getDbcData());
                                    MainForm.getInstance().updateUI(getDbcData());
                                }
                            });
                        }
                    }
                }
            };
        }
        return getProcesses;
    }

    public DbcData getDbcData() {
        return dbcData;
    }

    public void getProcesses() throws SQLException, IOException {
        clearProcessMap();
        if(connection == null) {
            return;
        }
        try (
                Statement stmt = connection.createStatement();
                ResultSet result = stmt.executeQuery(getQuery());
                ) {
            
            while(result.next()) {
                Process proc = new Process(
                        result.getInt(PID),
                        result.getString(APPLICATIONNAME),
                        result.getString(DATNAME),
                        result.getString(USENAME),
                        result.getString(CLIENT),
                        dateParse(result.getString(BACKENDSTART)),
                        dateParse(result.getString(QUERYSTART)),
                        dateParse(result.getString(XACTSTART)),
                        result.getString(STATE),
                        dateParse(result.getString(STATECHANGE)),
                        result.getInt(BLOCKEDBY),
                        result.getInt(BLOCKING_LOCKS),
                        result.getString(QUERYSQL),
                        result.getBoolean(SLOWQUERY));
                if (processMap.get(proc.getPid()) == null ||
                        processMap.get(proc.getPid()).getBlockedBy() != processMap.get(proc.getPid()).getBlockingLocks()) {
                    
                    getProcessMap().put(proc.getPid(), proc);
                }
            }
            setProcessList();
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
    
    public void terminate(int pid) {
        String term = "select pg_terminate_backend(?);";
        if(connection == null) {
            return;
        }
        boolean kill = false;
        try (PreparedStatement termPs = connection.prepareStatement(term);) {
            termPs.setInt(1, pid);
            try (ResultSet resultSet = termPs.executeQuery()) {
                if (resultSet.next()) {
                    kill = resultSet.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            getDbcData().setStatus(DbcStatus.ERROR);
            MainForm.getInstance().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MainForm.getInstance().serverStatusUpdate(getDbcData());
                }
            });
            LOG.error(getDbcData().getName() + " " + e.getMessage(), e);
        }
        if(kill) {
            LOG.info(String.format("%s pid = %s is terminated.", dbcData.getName(), pid));
        } else {
            LOG.info(String.format("%s pid = %s is terminated failed.", dbcData.getName(), pid));
        }
    }

    public void cancel(int pid) {
        String cancel = "select pg_cancel_backend(?);";
        if(connection == null) {
            return;
        }
        boolean kill = false;
        try(PreparedStatement cancelPs = connection.prepareStatement(cancel);) {
            cancelPs.setInt(1, pid);
            try (ResultSet resultSet = cancelPs.executeQuery()) {
                if (resultSet.next()) {
                    kill = resultSet.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            LOG.error(getDbcData().getName() + " " + e.getMessage(), e);
        }
        if(kill) {
            LOG.info(String.format("%s pid = %s is canceled.", dbcData.getName(), pid));
        } else {
            LOG.info(String.format("%s pid = %s is canceled failed.", dbcData.getName(), pid));
        }
    }

    public List<Process> getProcessList() {
        return processList;
    }
    public void setProcessList() {
        processList = new ProcessTreeList(getProcessMap()).getTreeList();
    }
    
    public boolean isConnected() {
        try {
            if(connection == null || connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Ошибка isConnected: " + e.getMessage());
        }
        return true;
    }
}
