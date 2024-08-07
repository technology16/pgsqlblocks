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
package ru.taximaxim.pgsqlblocks.common.models;

import java.util.Date;
import java.util.Objects;

/**
 * Когда DBProcessQuery создается внутри десериализации resultSet, невозможно
 * создать Duration. В связи с этим передается таймстамп, на основании которого вычисляется
 * длительность процесса.
 * <br>
 * Длительность процесса вычисляется на момент получения результат запроса.
 */
public class DBProcessQuery {

    private final String queryString;
    private final String queryFirstLine;
    private final boolean slowQuery;
    private final Date backendStart; //время подключения к серверу
    private final Date queryStart; //старт запроса
    private final Date xactStart; //старт транзакции
    private final String duration; //длительность запроса

    public DBProcessQuery(String queryString, boolean slowQuery, Date backendStart,
            Date queryStart, Date xactStart, String duration) {
        this.queryString = queryString == null ? "" : queryString;
        int indexOfNewLine = this.queryString.indexOf('\n');
        String substring = this.queryString.substring(0, indexOfNewLine >= 0 ? indexOfNewLine : this.queryString.length());
        this.queryFirstLine = indexOfNewLine >= 0 ? substring + " ..." : substring;

        this.slowQuery = slowQuery;
        this.backendStart = backendStart;
        this.queryStart = queryStart;
        this.xactStart = xactStart;
        this.duration = duration;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getQueryFirstLine() {
        return queryFirstLine;
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

    public String getDuration() {
        return duration;
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof DBProcessQuery)) {
            return false;
        }

        DBProcessQuery that = (DBProcessQuery) o;
        return Objects.equals(queryString, that.queryString)
                && slowQuery == that.slowQuery
                && Objects.equals(backendStart, that.backendStart)
                && Objects.equals(queryStart, that.queryStart)
                && Objects.equals(xactStart, that.xactStart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryString, slowQuery, backendStart, queryStart, xactStart);
    }
}