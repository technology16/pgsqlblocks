/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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
package ru.taximaxim.pgsqlblocks.blocksjournal;

import ru.taximaxim.pgsqlblocks.process.Process;

import java.util.*;
import java.util.stream.Collectors;

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

