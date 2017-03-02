package ru.taximaxim.pgsqlblocks.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Process implements Comparable<Process> {

    private List<Process> parents = new ArrayList<>();
    private final int pid;
    private final QueryCaller caller;
    private final String state;
    private final String stateChange;
    private final Set<Block> blocks = new HashSet<>();
    private final Query query;
    private final List<Process> children = new ArrayList<>();
    private ProcessStatus status = ProcessStatus.WORKING;
    private final boolean granted;

    public Process(int pid, QueryCaller caller, Query query, String state, String stateChange, boolean granted) {
        this.pid = pid;
        this.caller = caller;
        this.query = query;
        this.state = state  == null ? "" : state;
        this.stateChange = stateChange  == null ? "" : stateChange;
        this.granted = granted;
    }

    void setParents(Process parents) {
        this.parents.add(parents);
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public List<Process> getParents() {
        return parents;
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
        return !parents.isEmpty();
    }

    public String getState() {
        return state;
    }

    public String getStateChange() {
        return stateChange;
    }

    public Set<Block> getBlocks() {
        return blocks;
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public QueryCaller getCaller() {
        return caller;
    }

    public Query getQuery() {
        return query;
    }

    public int getChildrenCount() {
        return getChildren().size();
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public boolean isGranted() {
        return granted;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getPid();
        result = prime * result + (isGranted() ? 1 : 0);
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

        if (isGranted() ^ other.isGranted()) {
            return false;
        }
        return getPid() == other.getPid();
    }

    @Override
    public String toString() {
        return "Process{" +
                "parents=" + parents +
                ", pid=" + pid +
                ", caller=" + caller +
                ", state='" + state + '\'' +
                ", stateChange='" + stateChange + '\'' +
                ", blocks=" + blocks +
                ", query=" + query +
                ", children=" + children.size() +
                ", status=" + status +
                ", granted='" + granted + '\'' +
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
