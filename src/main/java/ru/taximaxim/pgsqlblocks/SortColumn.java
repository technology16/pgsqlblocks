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
package ru.taximaxim.pgsqlblocks;

import java.util.ResourceBundle;

public enum SortColumn {
    PID,
    BLOCKED_COUNT,
    APPLICATION_NAME,
    DATNAME,
    USENAME,
    CLIENT,
    BACKEND_START,
    QUERY_START,
    XACT_START,
    STATE,
    STATE_CHANGE,
    BLOCKED,
    LOCKTYPE,
    RELATION,
    QUERY,
    SLOWQUERY;

    /**
     * Получение имени колонки
     * @return String
     */
    public String getName(ResourceBundle resources) {
        switch (this) {
            case PID:
                return resources.getString("pid");
            case BLOCKED_COUNT:
                return resources.getString("num_of_blocked_processes");
            case APPLICATION_NAME:
                return resources.getString("application");
            case DATNAME:
                return resources.getString("db_name");
            case USENAME:
                return resources.getString("user_name");
            case CLIENT:
                return resources.getString("client");
            case BACKEND_START:
                return resources.getString("backend_start");
            case QUERY_START:
                return resources.getString("query_start");
            case XACT_START:
                return resources.getString("xact_start");
            case STATE:
                return resources.getString("state");
            case STATE_CHANGE:
                return resources.getString("state_change");
            case BLOCKED:
                return resources.getString("blocked_by");
            case LOCKTYPE:
                return resources.getString("lock_type");
            case RELATION:
                return resources.getString("relation");
            case QUERY:
                return resources.getString("query");
            case SLOWQUERY:
                return resources.getString("slow_query");
            default:
                return resources.getString("undefined");
        }
    }
    /**
     * Получение размера колонки
     * @return int
     */
    public int getColSize() {
        switch (this) {
            case STATE:
                return 55;
            case BLOCKED:
            case LOCKTYPE:
            case RELATION:
                return 70;
            case PID:
            case SLOWQUERY:
                return 80;
            case BLOCKED_COUNT:
            case DATNAME:
            case USENAME:
            case CLIENT:
                return 110;
            case BACKEND_START:
            case QUERY_START:
            case XACT_START:
            case STATE_CHANGE:
                return 145;
            case APPLICATION_NAME:
            case QUERY:
                return 150;
            default:
                return 100;
        }
    }
}
