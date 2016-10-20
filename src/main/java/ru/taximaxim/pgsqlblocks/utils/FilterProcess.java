package ru.taximaxim.pgsqlblocks.utils;

import org.apache.log4j.Logger;

import ru.taximaxim.pgsqlblocks.process.Process;

public final class FilterProcess {
    
    private static final Logger LOG = Logger.getLogger(FilterProcess.class);
    
    private FilterItem pid;
    
    private FilterItem dbName;
    
    private FilterItem userName;
    
    private FilterItem backendStart;
    
    private FilterItem queryStart;
    
    private static FilterProcess instance;
    
    private FilterProcess() {
        this.pid = new FilterItem("pid", "", "");
        this.dbName = new FilterItem("dbName", "", "");
        this.userName = new FilterItem("userName", "", "");
        this.backendStart = new FilterItem("backendStart", "", "");
        this.queryStart = new FilterItem("querystart", "", "");
    }
    
    public static FilterProcess getInstance() {
        if(instance == null) {
            instance = new FilterProcess();
        }
        return instance;
    }

    public void filterReset() {
        getPid().itemReset();
        getDbName().itemReset();
        getUserName().itemReset();
        getBackendStart().itemReset();
        getQueryStart().itemReset();
    }
    
    public boolean isFiltered(Process element) {
        try {
            boolean isCallerChanged = compareDbName(element) && compareUserName(element);
            boolean isQueryChanged = compareBackendStart(element) && compareQueryStart(element);
            return comparePid(element) && isCallerChanged && isQueryChanged;
        } catch (Exception e) {
            LOG.error("Некорректное выражение для фильтра!", e);
            return true;
        }
    }
    
    private boolean comparePid(Process element) {
        int pid = element.getPid();
        if (getPid().getValue().isEmpty()) {
            return true;
        }
        int fltPid = Integer.parseInt(getPid().getValue());
        if (getPid().getOperation() != null && !getPid().getOperation().isEmpty()) {
            switch (getPid().getOperation()) {
            case "=":
                return pid == fltPid;
            case "!=":
                return pid != fltPid;
            case ">":
                return pid > fltPid;
            case ">=":
                return pid >= fltPid;
            case "<":
                return pid < fltPid;
            case "<=":
                return pid <= fltPid;
            default:
                return true;
            }
        } else {
            return true;
        }
    }
    
    private boolean compareDbName(Process element) {
        String dbName = element.getCaller().getDatname();
        if (getDbName().getValue().isEmpty()) {
            return true;
        }
        String fltDbName = getDbName().getValue();
        if (getDbName().getOperation() != null && !getDbName().getOperation().isEmpty()) {
            switch (getDbName().getOperation()) {
            case "=":
                return dbName.equals(fltDbName);
            case "!=":
                return !dbName.equals(fltDbName);
            default:
                return true;
            }
        } else {
            return true;
        }
    }

    private boolean compareUserName(Process element) {
        String userName = element.getCaller().getUsername();
        if (getUserName().getValue().isEmpty()) {
            return true;
        }
        String fltUserName = getUserName().getValue();
        if (getUserName().getOperation() != null && !getUserName().getOperation().isEmpty()) {
            switch (getUserName().getOperation()) {
            case "=":
                return userName.equals(fltUserName);
            case "!=":
                return !userName.equals(fltUserName);
            default:
                return true;
            }
        } else {
            return true;
        }
    }
    
    private boolean compareBackendStart(Process element) {
        String backendStart = element.getQuery().getBackendStart();
        if (getBackendStart().getValue().isEmpty()) {
            return true;
        }
        String fltBackendStart = getBackendStart().getValue();
        if (getBackendStart().getOperation() != null && !getBackendStart().getOperation().isEmpty()) {
            switch (getBackendStart().getOperation()) {
            case "=":
                return backendStart.equals(fltBackendStart);
            case "!=":
                return !backendStart.equals(fltBackendStart);
            case ">":
                return backendStart.compareTo(fltBackendStart) > 0;
            case ">=":
                return backendStart.compareTo(fltBackendStart) >= 0;
            case "<":
                return backendStart.compareTo(fltBackendStart) < 0;
            case "<=":
                return backendStart.compareTo(fltBackendStart) >= 0;
            default:
                return true;
            }
        } else {
            return true;
        }
    }
    
    private boolean compareQueryStart(Process element) {
        String queryStart = element.getQuery().getQueryStart();
        if (getQueryStart().getValue().isEmpty()) {
            return true;
        }
        String fltQueryStart = getQueryStart().getValue();
        if (getQueryStart().getOperation() != null && !getQueryStart().getOperation().isEmpty()) {
            switch (getQueryStart().getOperation()) {
            case "=":
                return queryStart.equals(fltQueryStart);
            case "!=":
                return !queryStart.equals(fltQueryStart);
            case ">":
                return queryStart.compareTo(fltQueryStart) > 0;
            case ">=":
                return queryStart.compareTo(fltQueryStart) >= 0;
            case "<":
                return queryStart.compareTo(fltQueryStart) < 0;
            case "<=":
                return queryStart.compareTo(fltQueryStart) >= 0;
            default:
                return true;
            }
        } else {
            return true;
        }
    }
    
    /**
     * @return the pid
     */
    public FilterItem getPid() {
        return pid;
    }

    /**
     * @param pid the pid to set
     */
    public void setPid(FilterItem pid) {
        this.pid = pid;
    }

    /**
     * @return the dbName
     */
    public FilterItem getDbName() {
        return dbName;
    }

    /**
     * @param dbName the dbName to set
     */
    public void filterItem(FilterItem dbName) {
        this.dbName = dbName;
    }

    /**
     * @return the userName
     */
    public FilterItem getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(FilterItem userName) {
        this.userName = userName;
    }

    /**
     * @return the backendStart
     */
    public FilterItem getBackendStart() {
        return backendStart;
    }

    /**
     * @param backendStart the backendStart to set
     */
    public void setBackendStart(FilterItem backendStart) {
        this.backendStart = backendStart;
    }

    /**
     * @return the querystart
     */
    public FilterItem getQueryStart() {
        return queryStart;
    }

    /**
     * @param queryStart the querystart to set
     */
    public void setQueryStart(FilterItem queryStart) {
        this.queryStart = queryStart;
    }
}
