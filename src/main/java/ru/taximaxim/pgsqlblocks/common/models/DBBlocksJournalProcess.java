/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.common.models;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ru.taximaxim.treeviewer.models.IObject;

public class DBBlocksJournalProcess implements IObject {

    private final DBProcess process;

    private Date createDate;
    private Date closeDate;

    public DBBlocksJournalProcess(DBProcess process) {
        this.createDate = new Date();
        this.process = process;
    }

    public DBBlocksJournalProcess(Date createDate, Date closeDate, DBProcess process) {
        this.createDate = createDate;
        this.closeDate = closeDate;
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof DBBlocksJournalProcess)) {
            return false;
        }

        DBBlocksJournalProcess other = (DBBlocksJournalProcess) o;

        Date xactStart = process.getQuery().getXactStart();
        Date otherXactStart = other.getProcess().getQuery().getXactStart();

        boolean isSameXactStart = Objects.equals(xactStart, otherXactStart);
        boolean isSamePid = process.getPid() == other.getProcess().getPid();

        return isSameXactStart && isSamePid && childrenEquals(process.getChildren(), other.getProcess().getChildren());
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

    @Override
    public List<? extends IObject> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasChildren() {
        return false;
    }
}
