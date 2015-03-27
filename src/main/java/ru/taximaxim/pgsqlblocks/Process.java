package ru.taximaxim.pgsqlblocks;

import java.util.ArrayList;
import java.util.List;

public class Process {
    
    private Process parent;
    private List<Process> children = new ArrayList<Process>();
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
    private String query;
    private boolean slowQuery;
        
    public Process(int pid, String applicationName, String datname,
            String usename, String client, String backendStart,
            String queryStart, String xactStart, String state, String stateChange,
            int blockedBy, String query, boolean slowQuery) {
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
        this.query = query;
        this.slowQuery = slowQuery;
    }
    
    public void setParent(Process parent) {
        this.parent = parent;
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
        if(client == null)
            return "";
        return client;
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
                getBlockedBy()==0?"":String.valueOf(getBlockedBy()),
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
}