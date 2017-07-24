package ru.taximaxim.pgsqlblocks.common.models;

import java.util.*;

public class DBProcess {

    private List<DBProcess> parents = new ArrayList<>();
    private List<DBProcess> children = new ArrayList<>();

    private final Set<DBBlock> blocks = new HashSet<>();

    private final int pid;
    private final String state;
    private final Date stateChange;
    private final DBProcessQuery query;
    private final DBProcessQueryCaller queryCaller;

    private DBProcessStatus status = DBProcessStatus.WORKING;

    public DBProcess(int pid, DBProcessQueryCaller queryCaller, String state, Date stateChange, DBProcessQuery query) {
        this.pid = pid;
        this.queryCaller = queryCaller;
        this.state = state;
        this.stateChange = stateChange;
        this.query = query;
    }

    public void addBlock(DBBlock block) {
        blocks.add(block);
    }

    public List<DBProcess> getParents() {
        return parents;
    }

    public List<DBProcess> getChildren() {
        return children;
    }

    public void addChild(DBProcess process) {
        children.add(process);
    }

    public void addParent(DBProcess parentProcess) {
        this.parents.add(parentProcess);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean hasParent() {
        return !parents.isEmpty();
    }

    public DBProcessStatus getStatus() {
        return status;
    }

    public void setStatus(DBProcessStatus status) {
        this.status = status;
    }

    public Set<DBBlock> getBlocks() {
        return blocks;
    }

    public int getPid() {
        return pid;
    }

    public String getState() {
        return state;
    }

    public Date getStateChange() {
        return stateChange;
    }

    public DBProcessQuery getQuery() {
        return query;
    }

    public DBProcessQueryCaller getQueryCaller() {
        return queryCaller;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBProcess)) return false;

        DBProcess process = (DBProcess) o;

        return pid == process.pid;
    }

    @Override
    public int hashCode() {
        return pid;
    }
}
