package ru.taximaxim.pgsqlblocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private String getQuery() throws IOException {
        try (InputStream input = ClassLoader.getSystemResourceAsStream("query.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));)
        {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            return out.toString();
        }
    }
    
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
    public Runnable getProc() {
        return getProcesses;
    }
    private Runnable getProcesses = new Runnable() {
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
            } catch (IOException e) {
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

    public void getProcesses() throws SQLException, IOException {
        processMap = new ConcurrentHashMap<Integer, Process>();
        if(connection == null)
            return;
        Statement stmt = null;
        ResultSet result = null;
        stmt = connection.createStatement();
        result = stmt.executeQuery(getQuery());
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
                    result.getInt("blocking_locks"),
                    result.getString("query"),
                    result.getBoolean("slowquery"));
            if(processMap.get(proc.getPid())==null) {
                processMap.put(proc.getPid(), proc);
            } else {
                if(processMap.get(proc.getPid()).getBlockedBy()==processMap.get(proc.getPid()).getBlockingLocks()) {
                   processMap.put(proc.getPid(), proc);
                }
            }
        }
        result.close();
        stmt.close();
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
