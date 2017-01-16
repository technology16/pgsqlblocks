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
import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ru.taximaxim.pgsqlblocks.SortColumn;
import ru.taximaxim.pgsqlblocks.SortDirection;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcStatus;
import ru.taximaxim.pgsqlblocks.utils.Settings;

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

    private static final String QUERYFILENAME = "query.sql";
    private static final String QUERYWITHIDLEFILENAME = "query_with_idle.sql";

    private Settings settings = Settings.getInstance();
    
    private static String queryWithoutIdle;
    private static String queryWithIdle;
    private final DbcData dbcData;
    private final Process root = new Process(0, null, null, null, null);

    public ProcessTreeBuilder(DbcData dbcData) {
        this.dbcData = dbcData;
    }

    public Process buildProcessTree() {
        root.clearChildren();
        queryProcessTree().stream()
            .filter(process -> !process.hasParent())
            .forEach(root::addChildren);
        return root;
    }
    
    public Process buildOnlyBlockedProcessTree() {
        root.clearChildren();
        queryProcessTree().stream()
            .filter(Process::hasChildren)
            .forEach(root::addChildren);
        return root;
    }

    private Collection<Process> queryProcessTree() {
        Map<Integer, Process> tempProcessList = new HashMap<>();
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
            while (processSet.next()) {
                int pid = processSet.getInt(PID);
                Process currentProcess = tempProcessList.get(pid);
                if (currentProcess == null) {
                    Query query = new Query(processSet.getString(QUERYSQL),
                            dateParse(processSet.getString(BACKENDSTART)),
                            dateParse(processSet.getString(QUERYSTART)),
                            dateParse(processSet.getString(XACTSTART)),
                            processSet.getBoolean(SLOWQUERY));
                    QueryCaller caller = new QueryCaller(processSet.getString(APPLICATIONNAME),
                            processSet.getString(DATNAME),
                            processSet.getString(USENAME),
                            processSet.getString(CLIENT));
                    currentProcess = new Process(
                            pid,
                            caller,
                            query,
                            processSet.getString(STATE),
                            dateParse(processSet.getString(STATECHANGE)));
                    tempProcessList.put(pid, currentProcess);
                }
                
                int blockedBy = processSet.getInt(BLOCKEDBY);
                if (blockedBy != 0) {
                    currentProcess.addBlock(new Block(blockedBy, processSet.getString(LOCKTYPE), processSet.getString(RELATION)));
                }
            }
        } catch (SQLException e) {
            LOG.error(String.format("Ошибка при получении процессов для %s", dbcData.getDbname()), e);
        }
        
        // Пробегаем по списку процессов, ищем ожидающие и блокированные процессы
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

        return tempProcessList.values();
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
            case XACT_START:
                return stringCompare(process1.getQuery().getExactStart(), process2.getQuery().getExactStart(), sortDirection);
            case STATE:
                return stringCompare(process1.getState(), process2.getState(), sortDirection);
            case STATE_CHANGE:
                return stringCompare(process1.getStateChange(), process2.getStateChange(), sortDirection);
            case QUERY:
                return stringCompare(process1.getQuery().getQueryString(), process2.getQuery().getQueryString(), sortDirection);
            case SLOWQUERY:
                return getSortDirectionBySlowQueryColumn(sortDirection, process1, process2);
            case BLOCKED:
                return stringCompare(process1.getBlocks().toString(), process2.getBlocks().toString(), sortDirection);
            case LOCKTYPE:
                return stringCompare(
                        process1.getBlocks().stream().map(Block::getLocktype).collect(Collectors.joining(", ")),
                        process2.getBlocks().stream().map(Block::getLocktype).collect(Collectors.joining(", ")),
                        sortDirection);
            case RELATION:
                return stringCompare(
                    process1.getBlocks().stream().map(Block::getRelation).collect(Collectors.joining(", ")),
                    process2.getBlocks().stream().map(Block::getRelation).collect(Collectors.joining(", ")),
                    sortDirection);
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

    private Process getProcessByPid(Collection<Process> processList, int pid) {
        return processList.stream().filter(process -> process.getPid() == pid).findFirst().get();
    }

    private String getQuery(boolean showIdle) {
        if (showIdle) {
            queryWithIdle = (queryWithIdle == null) ? loadQuery(QUERYWITHIDLEFILENAME) : queryWithIdle;
            return queryWithIdle;
        } else {
            queryWithoutIdle = (queryWithoutIdle == null) ? loadQuery(QUERYFILENAME) : queryWithoutIdle;
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
