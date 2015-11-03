package ru.taximaxim.pgsqlblocks.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcStatus;

public class ProcessTreeBuilder {

    private static final Logger LOG = Logger.getLogger(ProcessTreeBuilder.class);

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
    
    private String query;
    private DbcData dbcData;
    private List<Process> tempProcessList = new ArrayList<Process>();

    private Process rootProcess;
    private Process blockedRootProcess;

    public ProcessTreeBuilder(DbcData dbcData) {
        this.dbcData = dbcData;
    }

    public Process getProcessTree() {
        rootProcess = new Process();
        
        buildProcessTree().stream()
            .filter(process -> !process.hasParent())
            .forEach(process -> rootProcess.addChildren(process));
        
        return rootProcess;
    }
    
    public Process getOnlyBlockedProcessTree() {
        blockedRootProcess = new Process();
        
        buildProcessTree().stream()
            .filter(process -> process.hasChildren())
            .forEach(process -> blockedRootProcess.addChildren(process));
        
        return blockedRootProcess;
    }

    public List<Process> buildProcessTree() {
        tempProcessList.clear();
        if (dbcData.getConnection() == null) {
            return tempProcessList;
        }
        try (
                PreparedStatement statement = dbcData.getConnection().prepareStatement(getQuery());
                ResultSet processSet = statement.executeQuery();
                ) {
            
            while (processSet.next()) {
                Process process = new Process(
                        processSet.getInt(PID),
                        processSet.getString(APPLICATIONNAME),
                        processSet.getString(DATNAME),
                        processSet.getString(USENAME),
                        processSet.getString(CLIENT),
                        dateParse(processSet.getString(BACKENDSTART)),
                        dateParse(processSet.getString(QUERYSTART)),
                        dateParse(processSet.getString(XACTSTART)),
                        processSet.getString(STATE),
                        dateParse(processSet.getString(STATECHANGE)),
                        processSet.getInt(BLOCKEDBY),
                        processSet.getInt(BLOCKING_LOCKS),
                        processSet.getString(QUERYSQL),
                        processSet.getBoolean(SLOWQUERY));

                tempProcessList.add(process);
            }
        } catch (SQLException e) {
            LOG.error(String.format("Ошибка при получении процессов для %s", dbcData.getDbname()), e);
        }
        
        // Пробегаем по списку процессов, ищем ожидающие и блокированные процессы
        dbcData.setStatus(DbcStatus.CONNECTED);
        for (Process process : tempProcessList) {
            if ((process.getBlockingLocks() != 0) && (process.getBlockedBy() == 0)) {
                //Добавляем для данного процесса родителя с pid = process.getBlockingLocks()
                Process parentProcess = getProcessByPid(tempProcessList, process.getBlockingLocks());
                if (parentProcess != null) {
                    process.setParent(parentProcess);
                    parentProcess.addChildren(process);
                    process.setStatus(ProcessStatus.WAITING);
                }
            } else if (process.getBlockedBy() != 0) {
                //Добавляем для данного процесса родителя с pid = process.getBlockedBy()
                Process parentProcess = getProcessByPid(tempProcessList, process.getBlockedBy());
                if (parentProcess != null) {
                    process.setParent(parentProcess);
                    parentProcess.addChildren(process);
                    parentProcess.setStatus(ProcessStatus.BLOCKING);
                    process.setStatus(ProcessStatus.BLOCKED);
                    dbcData.setStatus(DbcStatus.BLOCKED);
                }
            }
        }

        return tempProcessList;
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

    private Process getProcessByPid(List<Process> processList, int pid) {
        return processList.stream().filter(process -> process.getPid() == pid).findFirst().get();
    }
    
    private String getQuery() {
        if(query == null) {
            try (
                    InputStream input = ClassLoader.getSystemResourceAsStream("query.sql");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    ) {
                
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                query = out.toString();
            } catch (IOException e) {
                LOG.error("Ошибка чтения файла query.sql", e);
            }
        }
        return query;
    }
}
