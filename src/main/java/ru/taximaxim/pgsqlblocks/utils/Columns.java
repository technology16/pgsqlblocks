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
package ru.taximaxim.pgsqlblocks.utils;

public enum Columns {

    PID("pid"),
    BACKEND_TYPE("backend_type"),
    BLOCK_CREATE_DATE("block_start_date"),
    BLOCK_END_DATE("block_end_date"),
    BLOCKED_COUNT("num_of_blocked_processes"),
    APPLICATION_NAME("application"),
    DATABASE_NAME("db_name"),
    USER_NAME("user_name"),
    CLIENT("client"),
    BACKEND_START("backend_start"),
    QUERY_START("query_start"),
    XACT_START("xact_start"),
    DURATION("duration"),
    STATE("state"),
    STATE_CHANGE("state_change"),
    BLOCKED("blocked_by"),
    LOCK_TYPE("lock_type"),
    RELATION("relation"),
    SLOW_QUERY("slow_query"),
    QUERY("query");

    private final String columnName;

    Columns(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
