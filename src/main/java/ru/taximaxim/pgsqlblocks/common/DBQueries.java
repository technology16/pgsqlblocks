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
package ru.taximaxim.pgsqlblocks.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DBQueries {
    private static final Logger LOG = LogManager.getLogger(DBQueries.class);

    public static final String PG_BACKEND_PID_QUERY = "select pg_backend_pid();";

    private static String versionQuery;
    private static String processesQuery;
    private static String processesQueryForTen;
    private static String processesQueryWithIdle;
    private static String processesQueryWithIdleForTen;

    private static final String PROCESSES_QUERY_FILE_NAME = "query.sql";
    private static final String PROCESSES_QUERY_10_FILE_NAME = "query_10.sql";
    private static final String PROCESSES_QUERY_WITH_IDLE_FILE_NAME = "query_with_idle.sql";
    private static final String PROCESSES_QUERY_WITH_IDLE_10_FILE_NAME = "query_with_idle_10.sql";
    private static final String PG_SERVER_VERSION_QUERY_FILE_NAME = "version.sql";

    public static final String PG_TERMINATE_BACKEND_QUERY = "select pg_terminate_backend(?);";

    public static final String PG_CANCEL_BACKEND_QUERY = "select pg_cancel_backend(?);";

    public static synchronized String getProcessesQuery() {
        if (processesQuery == null) {
            processesQuery = loadQuery(PROCESSES_QUERY_FILE_NAME);
        }
        return processesQuery;
    }

    public static synchronized String getProcessesQueryForTen() {
        if (processesQueryForTen == null) {
            processesQueryForTen = loadQuery(PROCESSES_QUERY_10_FILE_NAME);
        }
        return processesQueryForTen;
    }

    public static synchronized String getProcessesQueryWithIdle() {
        if (processesQueryWithIdle == null) {
            processesQueryWithIdle = loadQuery(PROCESSES_QUERY_WITH_IDLE_FILE_NAME);
        }
        return processesQueryWithIdle;
    }

    public static synchronized String getProcessesQueryWithIdleForTen() {
        if (processesQueryWithIdleForTen == null) {
            processesQueryWithIdleForTen = loadQuery(PROCESSES_QUERY_WITH_IDLE_10_FILE_NAME);
        }
        return processesQueryWithIdleForTen;
    }

    public static synchronized String getVersionQuery(){
        if (versionQuery == null) {
            versionQuery = loadQuery(PG_SERVER_VERSION_QUERY_FILE_NAME);
        }
        return versionQuery;
    }

    private static String loadQuery(String queryFile) {
        try (InputStream input = ClassLoader.getSystemResourceAsStream(queryFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            return out.toString();
        } catch (IOException e) {
            LOG.error("Ошибка чтения файла " + queryFile, e);
            return null;
        }
    }

    private DBQueries() {
    }
}
