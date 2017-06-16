package ru.taximaxim.pgsqlblocks;

import java.util.Date;
import java.util.List;
import ru.taximaxim.pgsqlblocks.process.Process;

public class BlocksJournalNote {

    private final Date time;

    private final List<Process> processes;

    public BlocksJournalNote(List<Process> processes) {
        time = new Date();
        this.processes = processes;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public Date getTime() {
        return time;
    }

}
