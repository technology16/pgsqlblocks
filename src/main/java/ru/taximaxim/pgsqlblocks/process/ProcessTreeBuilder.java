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
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.taximaxim.pgsqlblocks.SortColumn;
import ru.taximaxim.pgsqlblocks.SortDirection;
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
    private final DbcData dbcData;
    private Set<Process> tempProcessList = new LinkedHashSet<Process>();
    private final Process root = new Process(0,null,null,null,null,0,0);

    public ProcessTreeBuilder(DbcData dbcData) {
        this.dbcData = dbcData;
    }

    public Process getProcessTree() {
        root.clearChildren();
        buildProcessTree().stream()
            .filter(process -> !process.hasParent())
            .forEach(root::addChildren);
        return root;
    }
    
    public Process getOnlyBlockedProcessTree() {
        root.clearChildren();
        buildProcessTree().stream()
            .filter(Process::hasChildren)
            .forEach(root::addChildren);
        return root;
    }

    private Set<Process> buildProcessTree() {
        tempProcessList.clear();
        if (dbcData.getConnection() == null) {
            return tempProcessList;
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
                PreparedStatement statement = dbcData.getConnection().prepareStatement(getQuery());
                ResultSet processSet = statement.executeQuery();
        ) {
            while (processSet.next()) {
                Query query = new Query(processSet.getString(QUERYSQL),
                        dateParse(processSet.getString(BACKENDSTART)),
                        dateParse(processSet.getString(QUERYSTART)),
                        dateParse(processSet.getString(XACTSTART)),
                        processSet.getBoolean(SLOWQUERY));
                QueryCaller caller = new QueryCaller(processSet.getString(APPLICATIONNAME),
                        processSet.getString(DATNAME),
                        processSet.getString(USENAME),
                        processSet.getString(CLIENT));
                Process process = new Process(
                        processSet.getInt(PID),
                        caller,
                        query,
                        processSet.getString(STATE),
                        dateParse(processSet.getString(STATECHANGE)),
                        processSet.getInt(BLOCKEDBY),
                        processSet.getInt(BLOCKING_LOCKS));
                tempProcessList.add(process);
            }
        } catch (SQLException e) {
            LOG.error(String.format("Ошибка при получении процессов для %s", dbcData.getDbname()), e);
        }
        
        // Пробегаем по списку процессов, ищем ожидающие и блокированные процессы
        dbcData.setStatus(DbcStatus.CONNECTED);
        dbcData.setContainBlockedProcess(false);
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
                    dbcData.setContainBlockedProcess(true);
                }
            }
        }

        return tempProcessList;
    }
    
    private int stringCompare(String s1, String s2, SortDirection sortDirection) {
        return sortDirection == SortDirection.DOWN ? s1.compareTo(s2) : s2.compareTo(s1);
    }
    
    public void processSort(Process rootProcess, SortColumn sortColumn, SortDirection sortDirection) {
        rootProcess.getChildren().sort((Process process1, Process process2) ->
                getSortDirectionByColumn(sortColumn, sortDirection, process1, process2));
    }

    private int getSortDirectionByColumn(SortColumn sortColumn, SortDirection sortDirection,
                                           Process process1, Process process2) {
        switch (sortColumn) {
            case PID:
                if(process1.getPid() > process2.getPid()) {
                    return sortDirection == SortDirection.UP ? -1 : 1;
                } else if(process1.getPid() < process2.getPid()) {
                    return sortDirection == SortDirection.UP ? 1 : -1;
                } else {
                    return 0;
                }
            case BLOCKED_COUNT:
                if(process1.getChildrenCount() > process2.getChildrenCount()) {
                    return sortDirection == SortDirection.UP ? -1 : 1;
                } else if(process1.getChildrenCount() < process2.getChildrenCount()) {
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
            case XACT_STAT:
                return stringCompare(process1.getQuery().getExactStart(), process2.getQuery().getExactStart(), sortDirection);
            case STATE:
                return stringCompare(process1.getState(), process2.getState(), sortDirection);
            case STATE_CHANGE:
                return stringCompare(process1.getStateChange(), process2.getStateChange(), sortDirection);
            case QUERY:
                return stringCompare(process1.getQuery().getQueryString(), process2.getQuery().getQueryString(), sortDirection);
            case SLOWQUERY:
                return getSortDirectionBySlowQueryColumn(sortDirection, process1, process2);
            case DEFAULT:
            case BLOCKED:
            case WAITING:
            default:
                return 0;
        }
    }
    // TODO refactor!
    private int getSortDirectionBySlowQueryColumn(SortDirection sortDirection, Process process1, Process process2) {
        if(sortDirection == SortDirection.UP) {
            if(process1.getQuery().isSlowQuery() && process2.getQuery().isSlowQuery()) {
                return 0;
            } else if(process1.getQuery().isSlowQuery() && !process2.getQuery().isSlowQuery()) {
                return 1;
            } else if(!process1.getQuery().isSlowQuery() && process2.getQuery().isSlowQuery()) {
                return -1;
            } else {
                return 0;
            }
        } else {
            if(process1.getQuery().isSlowQuery() && process2.getQuery().isSlowQuery()) {
                return 0;
            } else if(!process1.getQuery().isSlowQuery() && process2.getQuery().isSlowQuery()) {
                return 1;
            } else if(process1.getQuery().isSlowQuery() && !process2.getQuery().isSlowQuery()) {
                return -1;
            } else {
                return 0;
            }
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

    private Process getProcessByPid(Set<Process> processList, int pid) {
        return processList.stream().filter(process -> process.getPid() == pid).findFirst().get();
    }
    
    private String getQuery() {
        if(query == null) {
            try (
                    InputStream input = ClassLoader.getSystemResourceAsStream("query.sql");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
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
