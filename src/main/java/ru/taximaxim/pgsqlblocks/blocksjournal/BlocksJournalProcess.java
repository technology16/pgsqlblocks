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
