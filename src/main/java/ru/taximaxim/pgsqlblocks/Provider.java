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

import org.apache.log4j.Logger;

import ru.taximaxim.pgsqlblocks.ui.MainForm;

public class Provider {

    static {
        DriverManager.setLoginTimeout(5);
    }

    private static final String QUERY = "SELECT p.pid AS pid, application_name, datname, usename,"+
            "CASE WHEN client_port=-1 THEN 'local pipe' WHEN length(client_hostname)>0 THEN client_hostname||':'||client_port ELSE textin(inet_out(client_addr))||':'||client_port END AS client, "+
            "date_trunc('second', backend_start) AS backend_start, CASE WHEN state='active' THEN date_trunc('second', query_start)::text ELSE '' END AS query_start, "+
            "date_trunc('second', xact_start) AS xact_start, state, date_trunc('second', state_change) AS state_change, (SELECT min(l1.pid) " +
            "FROM pg_locks l1 WHERE GRANTED AND (relation IN (SELECT relation FROM pg_locks l2 WHERE l2.pid=p.pid AND NOT granted) OR transactionid IN (SELECT transactionid FROM pg_locks l3 WHERE l3.pid=p.pid AND NOT granted))) AS blockedby," +
            "query AS query, "+
            "CASE WHEN query_start IS NULL OR state<>'active' THEN false ELSE query_start < now() - '10 seconds'::interval END AS slowquery "+
            "FROM pg_stat_activity p ORDER BY 1 ASC";
    private static Logger log = Logger.getLogger(Provider.class);
    private DbcData dbcData;
    private Connection connection;
    private ConcurrentHashMap<Integer, Process> processMap;
    private List<Process> processList;

    public Provider(DbcData dbcData) {
        this.dbcData = dbcData;
    }

    private Runnable connect = new Runnable(){
        @Override
        public void run() {
            connect();
        }
    };
    
    public Runnable getConnection() {
        return connect;
    }
    
    private void connect(){
        if(isConnected()) {
            log.info(dbcData.getName() + " соединение уже создано");
            return;
        }
        try {
            log.info(dbcData.getName() + " Соединение...");
            connection = DriverManager.getConnection(dbcData.getUrl(), dbcData.getUser(), dbcData.getPasswd());
            dbcData.setStatus(DbcStatus.CONNECTED);
            log.info(dbcData.getName() + " Соединение создано.");
        } catch (SQLException e) {
            dbcData.setStatus(DbcStatus.ERROR);
            log.error(dbcData.getName() + " " + e.getMessage(), e);
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
                dbcData.setStatus(DbcStatus.DISABLED);
                log.info(dbcData.getName() + " Соединение закрыто.");
            } catch (SQLException e) {
                connection = null;
                dbcData.setStatus(DbcStatus.ERROR);
                log.error(dbcData.getName() + " " + e.getMessage(), e);
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

    public Runnable getProcesses = new Runnable() {
        @Override
        public void run() {
            if(!isConnected())
                return;
            try {
                getProcesses();
                if(processMap.size() > processList.size()) {
                    dbcData.setStatus(DbcStatus.BLOCKED);
                    for(Process process : processList) {
                        if(process.getChildren().size() > 0){
                            BlocksHistory.getInstance().add(dbcData, process);
                        }
                    }
                } else {
                    dbcData.setStatus(DbcStatus.CONNECTED);
                }
            } catch (SQLException e) {
                dbcData.setStatus(DbcStatus.ERROR);
                log.error(dbcData.getName() + " " + e.getMessage(), e);
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

    public DbcData getDbcData() {
        return dbcData;
    }

    public void getProcesses() throws SQLException {
        processMap = new ConcurrentHashMap<Integer, Process>();
        if(connection == null)
            return;
        Statement stmt = null;
        ResultSet result = null;
        stmt = connection.createStatement();
        result = stmt.executeQuery(QUERY);
        if(result == null)
            return;
        while(result.next()) {
            Process proc = new Process(
                    result.getInt("pid"),
                    result.getString("application_name"),
                    result.getString("datname"),
                    result.getString("usename"),
                    result.getString("client"),
                    dateParse(result.getString("backend_start")),
                    dateParse(result.getString("query_start")),
                    dateParse(result.getString("xact_start")),
                    result.getString("state"),
                    dateParse(result.getString("state_change")),
                    result.getInt("blockedby"),
                    result.getString("query"),
                    result.getBoolean("slowquery"));
            processMap.put(proc.getPid(), proc);
        }
        processList = new ProcessTreeList(processMap).getTreeList();
    }

    private String dateParse(String dateString) {
        if(dateString == null || dateString.length() == 0)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX");
        SimpleDateFormat sdfp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            log.error("Формат даты " + dateString + " не поддерживается", e);
        }
        return sdfp.format(date);

    }
    
    public void terminate(int pid) {
        String term = "select pg_terminate_backend(?);";
        if(connection == null) {
            return;
        }
        try {
            PreparedStatement termPs = connection.prepareStatement(term);
            termPs.setInt(1, pid);
            termPs.executeQuery();
            log.info(dbcData.getName() + " pid=" + pid + " is terminated.");
        } catch (SQLException e) {
            dbcData.setStatus(DbcStatus.ERROR);
            MainForm.getInstance().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MainForm.getInstance().serverStatusUpdate(getDbcData());
                }
            });
            log.error(dbcData.getName() + " " + e.getMessage(), e);
        }
    }

    public void cancel(int pid) {
        String cancel = "select pg_cancel_backend(?);";
        if(connection == null) {
            return;
        }
        try {
            PreparedStatement cancelPs = connection.prepareStatement(cancel);
            cancelPs.setInt(1, pid);
            cancelPs.executeQuery();
            log.info(dbcData.getName() + " pid=" + pid + " is canceled.");
        } catch (SQLException e) {
            log.error(dbcData.getName() + " " + e.getMessage(), e);
        }
    }

    public List<Process> getProcessList() {
        return processList;
    }

    public boolean isConnected() {
        try {
            if(connection == null || connection.isClosed())
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}
