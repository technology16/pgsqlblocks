package ru.taximaxim.pgsqlblocks.common.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBBlocksJournal {

    private final List<DBBlocksJournalListener> listeners = new ArrayList<>();

    private final List<DBBlocksJournalProcess> processes = new ArrayList<>();

    public List<DBBlocksJournalProcess> getProcesses() {
        return processes;
    }

    public void add(List<DBProcess> processes) {
        if (processes.isEmpty()) {
            closeProcesses();
            return;
        }
        if (this.processes.isEmpty()) {
            this.processes.addAll(processes.stream().map(DBBlocksJournalProcess::new).collect(Collectors.toList()));
            listeners.forEach(DBBlocksJournalListener::dbBlocksJournalDidAddProcesses);
        } else {
            List<DBBlocksJournalProcess> newProcesses = processes.stream()
                    .map(DBBlocksJournalProcess::new)
                    .collect(Collectors.toList());
            this.processes.stream()
                    .filter(DBBlocksJournalProcess::isOpened)
                    .filter(p -> !newProcesses.contains(p))
                    .collect(Collectors.toList())
                    .forEach(DBBlocksJournalProcess::close);
            for (DBBlocksJournalProcess process : newProcesses) {
                if (this.processes.contains(process)) {
                    DBBlocksJournalProcess prevJournalProcess = this.processes.get(this.processes.lastIndexOf(process));
                    if (prevJournalProcess.isClosed()) {
                        this.processes.add(process);
                    }
                } else {
                    this.processes.add(process);
                }
            }
            listeners.forEach(DBBlocksJournalListener::dbBlocksJournalDidAddProcesses);
        }
    }

    private void closeProcesses() {
        List<DBBlocksJournalProcess> openedProcesses = this.processes.stream()
                .filter(DBBlocksJournalProcess::isOpened)
                .collect(Collectors.toList());
        if (!openedProcesses.isEmpty()) {
            openedProcesses.forEach(DBBlocksJournalProcess::close);
            listeners.forEach(DBBlocksJournalListener::dbBlocksJournalDidCloseAllProcesses);
        }
    }

    public void clear() {
        processes.clear();
    }

    public int size() {
        return processes.size();
    }

    public boolean isEmpty() {
        return processes.isEmpty();
    }

    public void addListener(DBBlocksJournalListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBBlocksJournalListener listener) {
        listeners.remove(listener);
    }

}
