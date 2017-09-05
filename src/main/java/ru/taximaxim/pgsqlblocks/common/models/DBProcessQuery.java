/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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

import java.util.Date;

public class DBProcessQuery {

    private final String queryString;
    private final boolean slowQuery;
    private final Date backendStart;
    private final Date queryStart;
    private final Date xactStart;

    public DBProcessQuery(String queryString, boolean slowQuery, Date backendStart, Date queryStart, Date xactStart) {
        this.queryString = queryString == null ? "" : queryString;
        this.slowQuery = slowQuery;
        this.backendStart = backendStart;
        this.queryStart = queryStart;
        this.xactStart = xactStart;
    }

    public String getQueryString() {
        return queryString;
    }

    public boolean isSlowQuery() {
        return slowQuery;
    }

    public Date getBackendStart() {
        return backendStart;
    }

    public Date getQueryStart() {
        return queryStart;
    }

    public Date getXactStart() {
        return xactStart;
    }

    @Override
    public String toString() {
        return "DBProcessQuery{" +
                "queryString='" + queryString + '\'' +
                ", slowQuery=" + slowQuery +
                ", backendStart='" + backendStart + '\'' +
                ", queryStart='" + queryStart + '\'' +
                ", xactStart='" + xactStart + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBProcessQuery)) return false;

        DBProcessQuery that = (DBProcessQuery) o;

        if (slowQuery != that.slowQuery) return false;
        if (!queryString.equals(that.queryString)) return false;
        if (backendStart != null ? !backendStart.equals(that.backendStart) : that.backendStart != null) return false;
        if (queryStart != null ? !queryStart.equals(that.queryStart) : that.queryStart != null) return false;
        return xactStart != null ? xactStart.equals(that.xactStart) : that.xactStart == null;
    }

    @Override
    public int hashCode() {
        int result = queryString.hashCode();
        result = 31 * result + (slowQuery ? 1 : 0);
        result = 31 * result + (backendStart != null ? backendStart.hashCode() : 0);
        result = 31 * result + (queryStart != null ? queryStart.hashCode() : 0);
        result = 31 * result + (xactStart != null ? xactStart.hashCode() : 0);
        return result;
    }
}
