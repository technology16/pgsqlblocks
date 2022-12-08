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

import java.util.Objects;

public class DBModel {

    private final String name;
    private final String host;
    private final String port;
    private final String databaseName;
    private final String user;
    private final String password;
    private final boolean readBackendType;
    private final boolean enabled;

    public DBModel(String name, String host, String port, String databaseName,
            String user, String password, boolean readBackendType, boolean enabled) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.readBackendType = readBackendType;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public boolean isReadBackendType() {
        return readBackendType;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return !password.isEmpty();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public DBModel copy() {
        return new DBModel(this.name, this.host, this.port, this.databaseName,
                this.user, this.password, this.readBackendType, this.enabled);
    }

    @Override
    public String toString() {
        return "DBModel{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", readBackendType='" + readBackendType + '\'' +
                ", enabled=" + enabled +
                '}';
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof DBModel)) {
            return false;
        }

        DBModel other = (DBModel) obj;
        return Objects.equals(databaseName, other.databaseName)
                && enabled == other.enabled && Objects.equals(host, other.host)
                && Objects.equals(name, other.name)
                && Objects.equals(password, other.password)
                && Objects.equals(port, other.port)
                && readBackendType == other.readBackendType
                && Objects.equals(user, other.user);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((databaseName == null) ? 0 : databaseName.hashCode());
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result + (readBackendType ? 1231 : 1237);
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

}
