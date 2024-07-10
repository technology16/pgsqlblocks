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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ru.taximaxim.treeviewer.models.IObject;

public class DBProcess implements IObject {

    private List<DBProcess> parents = new ArrayList<>();
    private List<DBProcess> children = new ArrayList<>();

    private final Set<DBBlock> blocks = new HashSet<>();

    private final int pid;
    private final String state;
    private final String backendType;
    private final Date stateChange; //изменено
    private final DBProcessQuery query;
    private final DBProcessQueryCaller queryCaller;

    private DBProcessStatus status = DBProcessStatus.WORKING;

    public DBProcess(int pid, String backendType, DBProcessQueryCaller queryCaller, String state, Date stateChange, DBProcessQuery query) {
        this.pid = pid;
        this.backendType = backendType;
        this.queryCaller = queryCaller;
        this.state = state;
        this.stateChange = stateChange;
        this.query = query;
    }

    public void addBlock(DBBlock block) {
        blocks.add(block);
    }

    public List<DBProcess> getParents() {
        return parents;
    }

    public void addChild(DBProcess process) {
        children.add(process);
    }

    public void addParent(DBProcess parentProcess) {
        this.parents.add(parentProcess);
    }

    public boolean hasParent() {
        return !parents.isEmpty();
    }

    public DBProcessStatus getStatus() {
        return status;
    }

    public void setStatus(DBProcessStatus status) {
        this.status = status;
    }

    public Set<DBBlock> getBlocks() {
        return blocks;
    }

    public String getBlocksPidsString() {
        return blocks.stream()
                .map(b -> String.valueOf(b.getBlockingPid()))
                .collect(Collectors.joining(","));
    }

    public String getBlocksLocktypesString() {
        return blocks.stream()
                .map(DBBlock::getLocktype)
                .distinct()
                .collect(Collectors.joining(","));
    }

    public String getBlocksRelationsString() {
        return blocks.stream()
                .map(DBBlock::getRelation)
                .filter(r -> r != null && !r.isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }

    public int getPid() {
        return pid;
    }

    public String getBackendType() {
        return backendType;
    }

    public String getState() {
        return state;
    }

    public Date getStateChange() {
        return stateChange;
    }

    public DBProcessQuery getQuery() {
        return query;
    }

    public DBProcessQueryCaller getQueryCaller() {
        return queryCaller;
    }

    @Override
    public List<DBProcess> getChildren() {
        return children;
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBProcess)) return false;

        return pid == ((DBProcess) o).pid;
    }

    @Override
    public int hashCode() {
        return pid;
    }

    @Override
    public String toString() {
        return "DBProcess{" +
                "parents=" + parents +
                ", children=" + children.size() +
                ", blocks=" + blocks +
                ", pid=" + pid +
                ", state='" + state + '\'' +
                ", stateChange=" + stateChange +
                ", query=" + query +
                ", queryCaller=" + queryCaller +
                ", status=" + status +
                '}';
    }
}
