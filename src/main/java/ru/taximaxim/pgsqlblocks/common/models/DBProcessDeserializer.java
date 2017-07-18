package ru.taximaxim.pgsqlblocks.common.models;

import org.apache.log4j.Logger;

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
        String stateChangeDate = dateParse(resultSet.getString(STATE_CHANGE));

        String queryString = resultSet.getString(QUERY_SQL);
        String backendStart = dateParse(resultSet.getString(BACKEND_START));
        String queryStart = dateParse(resultSet.getString(QUERY_START));
        String xactStart = dateParse(resultSet.getString(XACT_START));
        boolean slowQuery = resultSet.getBoolean(SLOW_QUERY);

        DBProcessQuery query = new DBProcessQuery(queryString, slowQuery, backendStart, queryStart, xactStart);
        String appName = resultSet.getString(APPLICATION_NAME);
        String databaseName = resultSet.getString(DAT_NAME);
        String userName = resultSet.getString(USE_NAME);
        String client = resultSet.getString(CLIENT);
        DBProcessQueryCaller caller = new DBProcessQueryCaller(appName, databaseName, userName, client);

        return new DBProcess(pid, caller, state, stateChangeDate, query);
    }

    private String dateParse(String dateString) {
        if(dateString == null || dateString.length() == 0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX");
        SimpleDateFormat sdfp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            LOG.error("Формат даты " + dateString + " не поддерживается", e);
        }
        return sdfp.format(date);
    }

}
