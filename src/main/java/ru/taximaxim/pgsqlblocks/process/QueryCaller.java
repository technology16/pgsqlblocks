package ru.taximaxim.pgsqlblocks.process;

public class QueryCaller {
    private final String applicationName;
    private final String datname;
    private final String username;
    private final String client;

    QueryCaller(String applicationName, String datname, String username, String client) {
        this.applicationName = applicationName;
        this.datname = datname;
        this.username = username;
        this.client = client;
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
        return client == null ? "" : client;
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
