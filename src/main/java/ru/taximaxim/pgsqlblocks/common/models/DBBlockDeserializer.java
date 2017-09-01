package ru.taximaxim.pgsqlblocks.common.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBBlockDeserializer {

    private static final String BLOCKED_BY = "blockedBy";
    private static final String RELATION = "relation";
    private static final String LOCK_TYPE = "locktype";
    private static final String GRANTED = "granted";
    private static final String GRANTED_FLAG = "t";

    public DBBlock deserialize(ResultSet resultSet) throws SQLException {
        int blockedBy = resultSet.getInt(BLOCKED_BY);
        String lockType = resultSet.getString(LOCK_TYPE);
        String relation = resultSet.getString(RELATION);
        boolean granted = GRANTED_FLAG.equals(resultSet.getString(GRANTED));
        return new DBBlock(blockedBy, relation, lockType, granted);
    }
}
