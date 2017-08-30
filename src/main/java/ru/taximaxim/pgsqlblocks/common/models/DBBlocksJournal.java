package ru.taximaxim.pgsqlblocks.common.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBBlocksJournal implements DBProcessFilterListener {

    private final List<DBBlocksJournalListener> listeners = new ArrayList<>();

    private final List<DBBlocksJournalProcess> processes = new ArrayList<>();

    private final List<DBBlocksJournalProcess> filteredProcesses = new ArrayList<>();

    private final DBProcessFilter processesFilters = new DBProcessFilter();

    public List<DBBlocksJournalProcess> getProcesses() {
        return processes;
    }

    public List<DBBlocksJournalProcess> getFilteredProcesses() {
        return filteredProcesses;
    }

    public DBProcessFilter getProcessesFilters() {
        return processesFilters;
    }

    public DBBlocksJournal() {
        processesFilters.addListener(this);
    }

    public void add(List<DBProcess> processes) {
        if (processes.isEmpty()) {
            closeProcesses();
            return;
        }
        if (this.processes.isEmpty()) {
            this.processes.addAll(processes.stream().map(DBBlocksJournalProcess::new).collect(Collectors.toList()));
        } else {
            List<DBBlocksJournalProcess> newProcesses = processes.stream()
                    .map(DBBlocksJournalProcess::new)
                    .collect(Collectors.toList());
            List<DBBlocksJournalProcess> needCloseProcesses = this.processes.stream()
                    .filter(DBBlocksJournalProcess::isOpened)
                    .filter(p -> !newProcesses.contains(p))
                    .collect(Collectors.toList());
            if (!needCloseProcesses.isEmpty()) {
                needCloseProcesses.forEach(DBBlocksJournalProcess::close);
                listeners.forEach(listener -> listener.dbBlocksJournalDidCloseProcesses(needCloseProcesses));
            }
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
        }
        prepareFilteredProcesses();
        listeners.forEach(DBBlocksJournalListener::dbBlocksJournalDidAddProcesses);
    }

    public void setJournalProcesses(List<DBBlocksJournalProcess> processes) {
        clear();
        this.processes.addAll(processes);
        prepareFilteredProcesses();
        listeners.forEach(DBBlocksJournalListener::dbBlocksJournalDidAddProcesses);
    }

    private void prepareFilteredProcesses() {
        filteredProcesses.clear();
        if (processesFilters.isEnabled()) {
            filteredProcesses.addAll(processes.stream().filter(p -> processesFilters.filter(p.getProcess())).collect(Collectors.toList()));
        } else {
            filteredProcesses.addAll(processes);
        }
    }

    private void closeProcesses() {
        List<DBBlocksJournalProcess> openedProcesses = this.processes.stream()
                .filter(DBBlocksJournalProcess::isOpened)
                .collect(Collectors.toList());
        if (!openedProcesses.isEmpty()) {
            openedProcesses.forEach(DBBlocksJournalProcess::close);
            listeners.forEach(listener -> listener.dbBlocksJournalDidCloseProcesses(openedProcesses));
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

    @Override
    public void dbProcessFilterChanged() {
        prepareFilteredProcesses();
        listeners.forEach(DBBlocksJournalListener::dbBlocksJournalDidChangeFilters);
    }
}
