package ru.taximaxim.pgsqlblocks.process;

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

public class Block {

    private final int blockingPid;
    private final String relation;
    private final String locktype;

    public Block(int blockingPid, String locktype, String relation) {
        this.blockingPid = blockingPid;
        this.locktype = locktype == null ? "" : locktype;
        this.relation = relation == null ? "" : relation;
    }

    public String getLocktype() {
        return locktype;
    }

    public String getRelation() {
        return relation;
    }

    public int getBlockingPid() {
        return blockingPid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Block block = (Block) o;

        if (getBlockingPid() != block.getBlockingPid()) {
            return false;
        }
        if (getRelation() != null ? !getRelation().equals(block.getRelation()) : block.getRelation() != null) {
            return false;
        }
        return getLocktype().equals(block.getLocktype());
    }

    @Override
    public int hashCode() {
        int result = getBlockingPid();
        result = 31 * result + (getRelation() != null ? getRelation().hashCode() : 0);
        result = 31 * result + getLocktype().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockingPid=" + blockingPid +
                ", relation='" + relation + '\'' +
                ", locktype='" + locktype + '\'' +
                '}';
    }

}
