package ru.taximaxim.pgsqlblocks;

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

public class Provider {

    private static final String QUERY = "SELECT p.pid AS pid, application_name, datname, usename,"+
            "CASE WHEN client_port=-1 THEN 'local pipe' WHEN length(client_hostname)>0 THEN client_hostname||':'||client_port ELSE textin(inet_out(client_addr))||':'||client_port END AS client, "+
            "date_trunc('second', backend_start) AS backend_start, CASE WHEN state='active' THEN date_trunc('second', query_start)::text ELSE '' END AS query_start, "+
            "date_trunc('second', xact_start) AS xact_start, state, date_trunc('second', state_change) AS state_change, (SELECT min(l1.pid) " +
            "FROM pg_locks l1 WHERE GRANTED AND (relation IN (SELECT relation FROM pg_locks l2 WHERE l2.pid=p.pid AND NOT granted) OR transactionid IN (SELECT transactionid FROM pg_locks l3 WHERE l3.pid=p.pid AND NOT granted))) AS blockedby," +
            "query AS query, "+
            "CASE WHEN query_start IS NULL OR state<>'active' THEN false ELSE query_start < now() - '10 seconds'::interval END AS slowquery "+
            "FROM pg_stat_activity p ORDER BY 1 ASC";
    
    protected static final Logger LOG = Logger.getLogger(Provider.class);
    
    private static final String PID = "pid";
    private static final String DATNAME = "datname";
    private static final String USENAME = "usename";
    private static final String CLIENT = "client";
    private static final String STATE = "state";
    private static final String BLOCKEDBY = "blockedBy";
    private static final String SLOWQUERY = "slowQuery";
    private static final String APPLICATIONNAME = "application_name";
    private static final String BACKENDSTART = "backend_start";
    private static final String QUERYSTART = "query_start";
    private static final String XACTSTART = "xact_start";
    private static final String STATECHANGE = "state_change";
    private static final String QUERYSQL = "query";
    
    private DbcData dbcData;
    private Connection connection;
    private ConcurrentMap<Integer, Process> processMap;
    private List<Process> processList;
    private Runnable getProcesses;
    
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
            DriverManager.setLoginTimeout(5);
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

    public void getProcesses() throws SQLException {
        clearProcessMap();
        if(connection == null) {
            return;
        }
        try(Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(QUERY)){
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
                        result.getString(QUERYSQL),
                        result.getBoolean(SLOWQUERY));
                getProcessMap().put(proc.getPid(), proc);
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
        try (PreparedStatement termPs = connection.prepareStatement(term);){
            termPs.setInt(1, pid);
            termPs.executeQuery();
            LOG.info(getDbcData().getName() + " pid=" + pid + " is terminated.");
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
    }

    public void cancel(int pid) {
        String cancel = "select pg_cancel_backend(?);";
        if(connection == null) {
            return;
        }
        try (PreparedStatement cancelPs = connection.prepareStatement(cancel);){
            cancelPs.setInt(1, pid);
            cancelPs.executeQuery();
            LOG.info(getDbcData().getName() + " pid=" + pid + " is canceled.");
        } catch (SQLException e) {
            LOG.error(getDbcData().getName() + " " + e.getMessage(), e);
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
