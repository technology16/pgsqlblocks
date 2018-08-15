/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
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

import ru.taximaxim.pgsqlblocks.utils.SupportedVersion;

public class DBModel implements Cloneable {

    private String name;
    private String host;
    private String port;
    private SupportedVersion version;
    private String databaseName;
    private String user;
    private String password;
    private boolean enabled;

    public DBModel(String name, String host, String port, SupportedVersion version, String databaseName, String user, String password, boolean enabled) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.version = version;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
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

    public SupportedVersion getVersion() {
        return version;
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

    public DBModel clone() {
        return new DBModel(this.name, this.host, this.port, this.version, this.databaseName, this.user, this.password, this.enabled);
    }

    @Override
    public String toString() {
        return "DBModel{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", version='" + version + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBModel dbModel = (DBModel) o;

        if (enabled != dbModel.enabled) return false;
        if (name != null ? !name.equals(dbModel.name) : dbModel.name != null) return false;
        if (host != null ? !host.equals(dbModel.host) : dbModel.host != null) return false;
        if (port != null ? !port.equals(dbModel.port) : dbModel.port != null) return false;
        if (version != null ? !version.equals(dbModel.version) : dbModel.version != null) return false;
        if (databaseName != null ? !databaseName.equals(dbModel.databaseName) : dbModel.databaseName != null)
            return false;
        if (user != null ? !user.equals(dbModel.user) : dbModel.user != null) return false;
        return password != null ? password.equals(dbModel.password) : dbModel.password == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
