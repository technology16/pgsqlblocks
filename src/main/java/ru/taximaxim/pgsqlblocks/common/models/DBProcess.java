package ru.taximaxim.pgsqlblocks.common.models;

import java.util.ArrayList;
import java.util.List;

public class DBProcess {

    private List<DBProcess> parents = new ArrayList<>();
    private List<DBProcess> children = new ArrayList<>();



    private DBProcessStatus status = DBProcessStatus.WORKING;

    public List<DBProcess> getChildren() {
        return children;
    }

    public void addChild(DBProcess process) {
        children.add(process);
    }

    public boolean hasChilder() {
        return !children.isEmpty();
    }

    public DBProcessStatus getStatus() {
        return status;
    }

    public void setStatus(DBProcessStatus status) {
        this.status = status;
    }
}
