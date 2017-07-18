package ru.taximaxim.pgsqlblocks.common.models;

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
        if (this == o) return true;
        if (!(o instanceof DBProcessQueryCaller)) return false;

        DBProcessQueryCaller that = (DBProcessQueryCaller) o;

        if (!applicationName.equals(that.applicationName)) return false;
        if (!databaseName.equals(that.databaseName)) return false;
        if (!userName.equals(that.userName)) return false;
        return client.equals(that.client);
    }

    @Override
    public int hashCode() {
        int result = applicationName.hashCode();
        result = 31 * result + databaseName.hashCode();
        result = 31 * result + userName.hashCode();
        result = 31 * result + client.hashCode();
        return result;
    }
}
