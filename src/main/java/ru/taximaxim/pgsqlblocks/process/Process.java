package ru.taximaxim.pgsqlblocks.process;

import java.util.ArrayList;
import java.util.List;

public class Process implements Comparable<Process> {

    private Process parent;
    private final int pid;
    private final QueryCaller caller;
    private final String state;
    private final String stateChange;
    private final int blockedBy;
    private final int blockingLocks;
    private final Query query;
    private final List<Process> children = new ArrayList<>();
    private ProcessStatus status = ProcessStatus.WORKING;

    public Process(int pid, QueryCaller caller, Query query, String state, String stateChange, int blockedBy, int blockingLocks) {
        this.pid = pid;
        this.caller = caller;
        this.query = query;
        this.state = state;
        this.stateChange = stateChange;
        this.blockedBy = blockedBy;
        this.blockingLocks = blockingLocks;
    }

    void setParent(Process parent) {
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

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    boolean hasParent() {
        return parent != null;
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

    public QueryCaller getCaller() {
        return caller;
    }

    public Query getQuery() {
        return query;
    }

    public int getChildrenCount() {
        return getChildren().stream().mapToInt(Process::getChildrenCount).sum();
    }

    public ProcessStatus getStatus() {
        return status;
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
        return "Process{" +
                "parent=" + parent +
                ", pid=" + pid +
                ", caller=" + caller +
                ", state='" + state + '\'' +
                ", stateChange='" + stateChange + '\'' +
                ", blockedBy=" + blockedBy +
                ", blockingLocks=" + blockingLocks +
                ", query=" + query +
                ", children=" + children +
                ", status=" + status +
                '}';
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


}