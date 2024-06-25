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

public class DBBlock {

    private final int blockingPid;
    private final String relation;
    private final String locktype;
    private final boolean granted;

    public DBBlock(int blockingPid, String relation, String locktype, boolean granted) {
        this.blockingPid = blockingPid;
        this.relation = relation == null ? "" : relation;
        this.locktype = locktype == null ? "" : locktype;
        this.granted = granted;
    }

    public int getBlockingPid() {
        return blockingPid;
    }

    public String getRelation() {
        return relation;
    }

    public String getLocktype() {
        return locktype;
    }

    public boolean isGranted() {
        return granted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBBlock)) return false;

        DBBlock dbBlock = (DBBlock) o;

        if (blockingPid != dbBlock.blockingPid) return false;
        if (granted != dbBlock.granted) return false;
        if (!relation.equals(dbBlock.relation)) return false;
        return locktype.equals(dbBlock.locktype);
    }

    @Override
    public int hashCode() {
        int result = blockingPid;
        result = 31 * result + relation.hashCode();
        result = 31 * result + locktype.hashCode();
        result = 31 * result + (granted ? 1 : 0);
        return result;
    }
}
