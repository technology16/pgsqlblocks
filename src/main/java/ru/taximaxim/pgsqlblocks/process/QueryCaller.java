package ru.taximaxim.pgsqlblocks.process;

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

public class QueryCaller {
    private final String applicationName;
    private final String datname;
    private final String username;
    private final String client;

    QueryCaller(String applicationName, String datname, String username, String client) {
        this.applicationName = applicationName == null ? "" : applicationName;
        this.datname = datname == null ? "" : datname;
        this.username = username == null ? "" : username;
        this.client = client == null ? "" : client;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDatname() {
        return datname;
    }

    public String getUsername() {
        return username;
    }

    public String getClient() {
        return client ;
    }

    @Override
    public String toString() {
        return "QueryCaller{" +
                "applicationName='" + applicationName + '\'' +
                ", datname='" + datname + '\'' +
                ", username='" + username + '\'' +
                ", client='" + client + '\'' +
                '}';
    }
}
