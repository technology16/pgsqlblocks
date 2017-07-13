package ru.taximaxim.pgsqlblocks.blocksjournal;

import ru.taximaxim.pgsqlblocks.process.Process;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlocksJournal {

    private String dbcName;

    private List<BlocksJournalProcess> processes = new ArrayList<>();

    public BlocksJournal(String dbcName) {
        this.dbcName = dbcName;
    }

    public List<BlocksJournalProcess> getProcesses() {
        return processes;
    }

    public void clear() {
        processes.clear();
    }

    public void addProcesses(List<BlocksJournalProcess> newProcesses) {
        processes.stream()
                .filter(p -> !p.isClosed())
                .filter(p -> !newProcesses.contains(p))
                .collect(Collectors.toList())
                .forEach(BlocksJournalProcess::close);
        for (BlocksJournalProcess process : newProcesses) {
            if (processes.contains(process)) {
                BlocksJournalProcess prevJournalProcess = processes.get(processes.lastIndexOf(process));
                if (prevJournalProcess.isClosed()) {
                    processes.add(process);
                }
            } else {
                processes.add(process);
            }
        }
    }

    public void putProcesses(List<Process> newProcesses) {
        if (!newProcesses.isEmpty()) {
            List<BlocksJournalProcess> blocksJournalProcesses = newProcesses.stream().map(p-> new BlocksJournalProcess(dbcName, p)).collect(Collectors.toList());
            addProcesses(blocksJournalProcesses);
        }
        else if (!processes.isEmpty()) {
            closeProcesses();
        }
    }

    private void closeProcesses() {
        processes.stream().filter(p-> !(p.isClosed())).forEach(BlocksJournalProcess::close);
    }

}

