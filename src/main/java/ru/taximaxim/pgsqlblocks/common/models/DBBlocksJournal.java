/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
 * %
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.common.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBBlocksJournal{

    private final List<DBBlocksJournalListener> listeners = new ArrayList<>();

    private final List<DBBlocksJournalProcess> processes = new ArrayList<>();

    private final List<DBBlocksJournalProcess> filteredProcesses = new ArrayList<>();


    public List<DBBlocksJournalProcess> getProcesses() {
        return processes;
    }

    public List<DBBlocksJournalProcess> getFilteredProcesses() {
        return filteredProcesses;
    }

    public DBBlocksJournal() {
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
        filteredProcesses.addAll(processes);
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
}
