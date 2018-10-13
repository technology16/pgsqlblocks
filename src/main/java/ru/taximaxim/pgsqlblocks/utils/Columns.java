/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2018 "Technology" LLC
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
package ru.taximaxim.pgsqlblocks.utils;

public enum Columns {
    PID("pid", "PID", 80),
    BACKEND_TYPE("backend_type", "BACKEND_TYPE", 80),
    BLOCK_CREATE_DATE("block_start_date", "", 110),
    BLOCK_END_DATE("block_end_date", "", 150),
    BLOCKED_COUNT("num_of_blocked_processes", "", 70),
    APPLICATION_NAME("application", "APPLICATION_NAME", 100),
    DATABASE_NAME("db_name", "DATABASE_NAME", 110),
    USER_NAME("user_name", "USER_NAME", 110),
    CLIENT("client", "CLIENT", 100),
    BACKEND_START("backend_start", "BACKEND_START", 110),
    QUERY_START("query_start", "QUERY_START", 110),
    XACT_START("xact_start", "XACT_START", 150),
    DURATION("duration", "now - XACT_START", 70),
    STATE("state", "STATE", 70),
    STATE_CHANGE("state_change", "STATE_CHANGE", 150),
    BLOCKED("blocked_by", "BLOCKED", 110),
    LOCK_TYPE("lock_type", "LOCK_TYPE", 110),
    RELATION("relation", "RELATION", 130),
    SLOW_QUERY("slow_query", "SLOW_QUERY", 150),
    QUERY("query", "QUERY", 100);

    private final String columnName;
    private final String columnTooltip;
    private final int columnWidth;

    Columns(String columnName, String columnTooltip, int columnWidth) {
        this.columnName = columnName;
        this.columnTooltip = columnTooltip;
        this.columnWidth = columnWidth;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnTooltip() {
        return columnTooltip;
    }

    public int getColumnWidth() {
        return columnWidth;
    }
}
