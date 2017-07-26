package ru.taximaxim.pgsqlblocks.common;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class DBQueries {
    private static final Logger LOG = Logger.getLogger(DBQueries.class);

    public final static String PG_BACKEND_PID_QUERY = "select pg_backend_pid();";

    private static String processesQuery;
    private static String processesQueryWithIdle;

    private static final String PROCESSES_QUERY_FILE_NAME = "query.sql";
    private static final String PROCESSES_QUERY_WITH_IDLE_FILE_NAME = "query_with_idle.sql";

    public static final String PG_TERMINATE_BACKED_QUERY = "select pg_terminate_backend(?);";

    public static final String PG_CANCEL_BACKED_QUERY = "select pg_cancel_backend(?);";

    public synchronized static String getProcessesQuery() {
        if (processesQuery == null) {
            processesQuery = loadQuery(PROCESSES_QUERY_FILE_NAME);
        }
        return processesQuery;
    }

    public synchronized static String getProcessesQueryWithIdle() {
        if (processesQueryWithIdle == null) {
            processesQueryWithIdle = loadQuery(PROCESSES_QUERY_WITH_IDLE_FILE_NAME);
        }
        return processesQueryWithIdle;
    }

    private static String loadQuery(String queryFile) {
        try (InputStream input = ClassLoader.getSystemResourceAsStream(queryFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
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
}
