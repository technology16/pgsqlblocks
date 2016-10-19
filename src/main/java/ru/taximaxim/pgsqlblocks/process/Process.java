package ru.taximaxim.pgsqlblocks.process;

import java.util.ArrayList;
import java.util.List;

public class Process implements Comparable<Process> {
    
    private Process parent;
    private int pid;
    private String applicationName;
    private String datname;
    private String usename;
    private String client;
    private String backendStart;
    private String queryStart;
    private String xactStart;
    private String state;
    private String stateChange;
    private int blockedBy;
    private int blockingLocks;
    private String query;
    private boolean slowQuery;
    private List<Process> children = new ArrayList<Process>();
    private ProcessStatus status = ProcessStatus.WORKING;
    
    public Process() {}
    
    public Process(int pid, String applicationName, String datname,
            String usename, String client, String backendStart,
            String queryStart, String xactStart, String state, String stateChange,
            int blockedBy, int blockingLocks, String query, boolean slowQuery) {
        
        this.pid = pid;
        this.applicationName = applicationName;
        this.datname = datname;
        this.usename = usename;
        this.client = client;
        this.backendStart = backendStart;
        this.queryStart = queryStart;
        this.xactStart = xactStart;
        this.state = state;
        this.stateChange = stateChange;
        this.blockedBy = blockedBy;
        this.blockingLocks = blockingLocks;
        this.query = query;
        this.slowQuery = slowQuery;
    }
    
    public void setParent(Process parent) {
        this.parent = parent;
    }
    
    public void setStatus(ProcessStatus status) {
        this.status = status;
    }
    
    public Process getParent() {
        return parent;
    }
    
    public void addChildren(Process child) {
        children.add(child);
    }
    
    public List<Process> getChildren() {
        return children;
    }
    
    void clearChildren(){
        children.clear();
    }

    public int getPid() {
        return pid;
    }
    
    public String getApplicationName() {
        return applicationName;
    }
    
    public String getDatname() {
        return datname;
    }
    
    public String getUsename() {
        return usename;
    }
    
    public String getClient() {
        if(client == null) {
            return "";
        }
        return client;
    }
    
    public boolean hasChildren() {
        if (children.size() > 0) {
            return true;
        }
        return false;
    }
    
    public boolean hasParent() {
        if (parent != null) {
            return true;
        }
        return false;
    }
    
    public String getBackendStart() {
        return backendStart;
    }
    
    public String getQueryStart() {
        return queryStart;
    }
    
    public String getXactStart() {
        return xactStart;
    }
    
    public String getState() {
        return state;
    }
    
    public String getStateChange() {
        return stateChange;
    }
    
    public int getBlockedBy() {
        return blockedBy;
    }
    
    public int getBlockingLocks() {
        return blockingLocks;
    }
    
    public String getQuery() {
        return query;
    }
    
    public boolean isSlowQuery() {
        return slowQuery;
    }
    
    public int getChildrensCount() {
        int count = getChildren().size();
        if(count == 0) {
            return count;
        }
        for(Process proc : getChildren()) {
            count += proc.getChildrensCount();
        }
        return count;
    }
    
    public ProcessStatus getStatus() {
        return status;
    }
    
    public String[] toTree() {
        return new String[]{
                String.valueOf(getPid()),
                String.valueOf(getChildrensCount()),
                getApplicationName(),
                getDatname(),
                getUsename(),
                getClient(),
                getBackendStart(),
                getQueryStart(),
                getXactStart(),
                getState(),
                getStateChange(),
                getBlockedBy() == 0 ? "" : String.valueOf(getBlockedBy()),
                getQuery().replace("\n", " "),
                String.valueOf(isSlowQuery())
        };
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getPid();
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        Process other = (Process) obj;
        if (getPid() != other.getPid()){
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("Process [parent=%1$s, childrenLength=%2$s, pid=%3$s, applicationName=%4$s, datname=%5$s, " +
                "usename=%6$s, client=%7$s, backendStart=%8$s, queryStart=%9$s, xactStart=%10$s, state=%11$s, stateChange=%12$s, " +
                "blockedBy=%13$s, query=%14$s, slowQuery=%15$s]", 
                getParent(), getChildren().size(), getPid(), getApplicationName(), getDatname(), getUsename(), getClient(), 
                getBackendStart(), getQueryStart(), getXactStart(), getState(), getStateChange(), getBlockedBy(), "query", isSlowQuery());
    }
    
    @Override
    public int compareTo(Process other) {
        if (pid == other.getPid()) {
            return 0;
        } else if (pid > other.getPid()) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public void removeChild(Process process) {
        children.remove(process);
    }
}