package ru.taximaxim.pgsqlblocks.common.models;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBProcessDeserializer {

    private static final Logger LOG = Logger.getLogger(DBProcessDeserializer.class);

    private static final String PID = "pid";
    private static final String STATE = "state";
    private static final String STATE_CHANGE = "state_change";
    private static final String QUERY_SQL = "query";
    private static final String BACKEND_START = "backend_start";
    private static final String SLOW_QUERY = "slowQuery";
    private static final String QUERY_START = "query_start";
    private static final String XACT_START = "xact_start";
    private static final String APPLICATION_NAME = "application_name";
    private static final String DAT_NAME = "datname";
    private static final String USE_NAME = "usename";
    private static final String CLIENT = "client";

    public DBProcess deserialize(ResultSet resultSet) throws SQLException {
        int pid = resultSet.getInt(PID);
        String state = resultSet.getString(STATE);
        Date stateChangeDate = DateUtils.dateFromString(resultSet.getString(STATE_CHANGE));

        String queryString = resultSet.getString(QUERY_SQL);
        Date backendStart = DateUtils.dateFromString(resultSet.getString(BACKEND_START));
        Date queryStart = DateUtils.dateFromString(resultSet.getString(QUERY_START));
        Date xactStart = DateUtils.dateFromString(resultSet.getString(XACT_START));

        boolean slowQuery = resultSet.getBoolean(SLOW_QUERY);

        DBProcessQuery query = new DBProcessQuery(queryString, slowQuery, backendStart, queryStart, xactStart);
        String appName = resultSet.getString(APPLICATION_NAME);
        String databaseName = resultSet.getString(DAT_NAME);
        String userName = resultSet.getString(USE_NAME);
        String client = resultSet.getString(CLIENT);
        DBProcessQueryCaller caller = new DBProcessQueryCaller(appName, databaseName, userName, client);

        return new DBProcess(pid, caller, state, stateChangeDate, query);
    }

}
