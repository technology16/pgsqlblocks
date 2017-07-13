package ru.taximaxim.pgsqlblocks.blocksjournal;

import ru.taximaxim.pgsqlblocks.process.Process;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlocksJournalProcess {

    private Date createDate;
    private Date closeDate;

    private String dbcName;

    private Process process;

    public BlocksJournalProcess(String dbcName, Process process) {
        createDate = new Date();
        this.dbcName = dbcName;
        this.process = process;
    }

    public Process getProcess() {
        return process;
    }

    public void close() {
        if (!isClosed()) {
            closeDate = new Date();
        }
    }

    public String getDbcName() {
        return dbcName;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public boolean isClosed() {
        return closeDate != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlocksJournalProcess)) return false;

        BlocksJournalProcess other = (BlocksJournalProcess) o;

        String xactStart = process.getQuery().getXactStart();
        String otherXactStart = other.getProcess().getQuery().getXactStart();

        if (!(xactStart != null ? xactStart.equals(otherXactStart) : otherXactStart == null)) {
            return false;
        }

        if (process.getPid() != other.getProcess().getPid()) {
            return false;
        }
        return childrenEquals(process.getChildren(), other.getProcess().getChildren());
    }

    private boolean childrenEquals(List<Process> processes, List<Process> other) {
        if (processes.size() != other.size()) {
            return false;
        }
        Set<Integer> processChildrenPids = processes.stream().map(Process::getPid).collect(Collectors.toSet());
        Set<Integer> otherProcessChildrenPids = other.stream().map(Process::getPid).collect(Collectors.toSet());
        if (!processChildrenPids.equals(otherProcessChildrenPids)) {
            return false;
        }
        for (Process process : processes) {
            Process sameProcess = other.stream().filter(p -> p.getPid() == process.getPid()).collect(Collectors.toList()).get(0);
            String xactStart = process.getQuery().getXactStart();
            String otherXactStart = sameProcess.getQuery().getXactStart();
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
        String xactStart = process.getQuery().getXactStart();
        int result = 31 * process.getPid();
        result = 31 * result + (xactStart != null ? xactStart.hashCode() : 0);
        result = childrenHashCode(result, process.getChildren());
        return result;
    }

    private int childrenHashCode(int hashCode, List<Process> children) {
        int result = hashCode;
        for (Process process : children) {
            result = 31 * result + process.getPid();
            result = childrenHashCode(result, process.getChildren());
        }
        return result;
    }

}
