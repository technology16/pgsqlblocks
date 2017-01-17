package ru.taximaxim.pgsqlblocks.process;

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

    String getApplicationName() {
        return applicationName;
    }

    public String getDatname() {
        return datname;
    }

    public String getUsername() {
        return username;
    }

    String getClient() {
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
