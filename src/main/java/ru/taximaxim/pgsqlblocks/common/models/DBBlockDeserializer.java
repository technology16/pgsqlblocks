/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2022 "Technology" LLC
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
