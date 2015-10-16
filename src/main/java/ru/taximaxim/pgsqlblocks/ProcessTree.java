package ru.taximaxim.pgsqlblocks;

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
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class ProcessTree {

    private static final Logger LOG = Logger.getLogger(ProcessTree.class);

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

    private TreeSet<Process> processTree = new TreeSet<Process>();

    public ProcessTree(DbcData dbcData) {
        this.dbcData = dbcData;
    }

    public TreeSet<Process> getProcessTree() {
        return createProcessTree();
    }

    public TreeSet<Process> createProcessTree() {
        processTree.clear();
        tempProcessList.clear();
        if (dbcData.getConnection() == null) {
            return new TreeSet<Process>();
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
                }
            }
        }

        // Добавляем в дерево только корневые элементы
        for (Process process : tempProcessList) {
            if (!process.hasParent()) {
                processTree.add(process);
            }
        }

        return processTree;
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
        for (Process process : processList) {
            if (process.getPid() == pid) {
                return process;
            }
        }
        return null;
    }
    
   /* private Process getProcessByBlocked(List<Process> processList, int blockedBy) {
        for (Process process : processList) {
            if (process.getBlockedBy() == blockedBy) {
                return process;
            }
        }
        return null;
    }

    private Process getProcessByBlocking(List<Process> processList, int blockingLocks) {
        for (Process process : processList) {
            if (process.getBlockingLocks() == blockingLocks) {
                return process;
            }
        }
        return null;
    }*/

    /*private void buildTree() {
        for(Entry<Integer, Process> map : getProcessMap().entrySet()) {
            int blockedBy = map.getValue().getBlockedBy();
            int blockingLocks = map.getValue().getBlockingLocks();

            if(blockedBy != 0) {
                map.getValue().setParent(getProcessMap().get(blockedBy));
                getProcessMap().get(blockedBy).addChildren(map.getValue());
            }
            if((blockingLocks != 0) && (blockingLocks != blockedBy)) {
                map.getValue().setParent(getProcessMap().get(blockingLocks));
                getProcessMap().get(blockingLocks).addChildren(map.getValue());
            }
        }
        for(Entry<Integer, Process> map : getProcessMap().entrySet()) {
            if((map.getValue().getBlockedBy() == 0) && (map.getValue().getBlockingLocks() == 0) ) {
                getTreeList().add(map.getValue());
            }
        }
    }

    public ConcurrentMap<Integer, Process> getProcessMap() {
        return processMap;
    }

    public List<Process> getTreeList() {
        if (processList == null) {
            processList = new ArrayList<Process>();
        }
        return processList;
    }*/
    
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
