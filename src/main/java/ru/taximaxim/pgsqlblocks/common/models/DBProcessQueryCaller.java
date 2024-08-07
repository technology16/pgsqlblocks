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

import java.util.Objects;

public class DBProcessQueryCaller {

    private final String applicationName;
    private final String databaseName;
    private final String userName;
    private final String client;

    public DBProcessQueryCaller(String applicationName, String databaseName, String userName, String client) {
        this.applicationName = applicationName == null ? "" : applicationName;
        this.databaseName = databaseName == null ? "" : databaseName;
        this.userName = userName == null ? "" : userName;
        this.client = client == null ? "" : client;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUserName() {
        return userName;
    }

    public String getClient() {
        return client;
    }

    @Override
    public String toString() {
        return "DBProcessQueryCaller{" +
                "applicationName='" + applicationName + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", userName='" + userName + '\'' +
                ", client='" + client + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DBProcessQueryCaller)) {
            return false;
        }

        DBProcessQueryCaller that = (DBProcessQueryCaller) o;
        return Objects.equals(applicationName, that.applicationName)
                && Objects.equals(databaseName, that.databaseName)
                && Objects.equals(userName, that.userName)
                && Objects.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, databaseName, userName, client);
    }
}
