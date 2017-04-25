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
package ru.taximaxim.pgsqlblocks.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Process implements Comparable<Process> {

    private List<Process> parents = new ArrayList<>();
    private final int pid;
    private final QueryCaller caller;
    private final String state;
    private final String stateChange;
    private final Set<Block> blocks = new HashSet<>();
    private final Query query;
    private final List<Process> children = new ArrayList<>();
    private ProcessStatus status = ProcessStatus.WORKING;

    public Process(int pid, QueryCaller caller, Query query, String state, String stateChange) {
        this.pid = pid;
        this.caller = caller;
        this.query = query;
        this.state = state  == null ? "" : state;
        this.stateChange = stateChange  == null ? "" : stateChange;
    }

    void setParents(Process parents) {
        this.parents.add(parents);
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public List<Process> getParents() {
        return parents;
    }

    public void addChildren(Process child) {
        children.add(child);
    }

    public List<Process> getChildren() {
        return children;
    }

    void clearChildren(){
        children.clear();
    }

    public int getPid() {
        return pid;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    boolean hasParent() {
        return !parents.isEmpty();
    }

    public String getState() {
        return state;
    }

    public String getStateChange() {
        return stateChange;
    }

    public Set<Block> getBlocks() {
        return blocks;
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public QueryCaller getCaller() {
        return caller;
    }

    public Query getQuery() {
        return query;
    }

    public int getChildrenCount() {
        return getChildren().size();
    }

    public ProcessStatus getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getPid();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        Process other = (Process) obj;

        return getPid() == other.getPid();
    }

    @Override
    public String toString() {
        return "Process{" +
                "parents=" + parents +
                ", pid=" + pid +
                ", caller=" + caller +
                ", state='" + state + '\'' +
                ", stateChange='" + stateChange + '\'' +
                ", blocks=" + blocks +
                ", query=" + query +
                ", children=" + children.size() +
                ", status=" + status +
                '}';
    }

    @Override
    public int compareTo(Process other) {
        if (pid == other.getPid()) {
            return 0;
        } else if (pid > other.getPid()) {
            return 1;
        } else {
            return -1;
        }
    }
}
