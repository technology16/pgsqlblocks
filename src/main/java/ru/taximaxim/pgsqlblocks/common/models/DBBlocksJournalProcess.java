package ru.taximaxim.pgsqlblocks.common.models;


import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DBBlocksJournalProcess {

    private final DBProcess process;

    private Date createDate;
    private Date closeDate;

    public DBBlocksJournalProcess(DBProcess process) {
        this.createDate = new Date();
        this.process = process;
    }

    public DBProcess getProcess() {
        return process;
    }

    public boolean isClosed() {
        return closeDate != null;
    }

    public boolean isOpened() {
        return closeDate == null;
    }

    public void close() {
        if (!isClosed()) {
            closeDate = new Date();
        }
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBBlocksJournalProcess)) return false;

        DBBlocksJournalProcess other = (DBBlocksJournalProcess) o;

        Date xactStart = process.getQuery().getXactStart();
        Date otherXactStart = other.getProcess().getQuery().getXactStart();

        if (!(xactStart != null ? xactStart.equals(otherXactStart) : otherXactStart == null)) {
            return false;
        }

        if (process.getPid() != other.getProcess().getPid()) {
            return false;
        }
        return childrenEquals(process.getChildren(), other.getProcess().getChildren());
    }

    private boolean childrenEquals(List<DBProcess> processes, List<DBProcess> other) {
        if (processes.size() != other.size()) {
            return false;
        }
        Set<Integer> processChildrenPids = processes.stream().map(DBProcess::getPid).collect(Collectors.toSet());
        Set<Integer> otherProcessChildrenPids = other.stream().map(DBProcess::getPid).collect(Collectors.toSet());
        if (!processChildrenPids.equals(otherProcessChildrenPids)) {
            return false;
        }
        for (DBProcess process : processes) {
            DBProcess sameProcess = other.stream().filter(p -> p.getPid() == process.getPid()).collect(Collectors.toList()).get(0);
            Date xactStart = process.getQuery().getXactStart();
            Date otherXactStart = sameProcess.getQuery().getXactStart();
            if (!(xactStart != null ? xactStart.equals(otherXactStart) : otherXactStart == null)) {
                return false;
            }
            if (!childrenEquals(process.getChildren(), sameProcess.getChildren())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        Date xactStart = process.getQuery().getXactStart();
        int result = 31 * process.getPid();
        result = 31 * result + (xactStart != null ? xactStart.hashCode() : 0);
        result = childrenHashCode(result, process.getChildren());
        return result;
    }

    private int childrenHashCode(int hashCode, List<DBProcess> children) {
        int result = hashCode;
        for (DBProcess process : children) {
            result = 31 * result + process.getPid();
            result = childrenHashCode(result, process.getChildren());
        }
        return result;
    }
}
