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

import java.util.*;
import java.util.stream.Collectors;

public class DBProcess {

    private List<DBProcess> parents = new ArrayList<>();
    private List<DBProcess> children = new ArrayList<>();

    private final Set<DBBlock> blocks = new HashSet<>();

    private final int pid;
    private final String state;
    private final Date stateChange;
    private final DBProcessQuery query;
    private final DBProcessQueryCaller queryCaller;

    private DBProcessStatus status = DBProcessStatus.WORKING;

    public DBProcess(int pid, DBProcessQueryCaller queryCaller, String state, Date stateChange, DBProcessQuery query) {
        this.pid = pid;
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

    public List<DBProcess> getChildren() {
        return children;
    }

    public void addChild(DBProcess process) {
        children.add(process);
    }

    public void addParent(DBProcess parentProcess) {
        this.parents.add(parentProcess);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBProcess)) return false;

        DBProcess process = (DBProcess) o;

        return pid == process.pid;
    }

    @Override
    public int hashCode() {
        return pid;
    }

    @Override
    public String toString() {
        return "DBProcess{" +
                "parents=" + parents +
                ", children=" + children +
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
