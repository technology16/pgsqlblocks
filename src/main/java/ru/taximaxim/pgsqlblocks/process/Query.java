package ru.taximaxim.pgsqlblocks.process;

public class Query {
    private final String queryString;
    private final boolean slowQuery;
    private final String backendStart;
    private final String queryStart;
    private final String exactStart;

    Query(String queryString, String backendStart, String queryStart, String exactStart, boolean slowQuery) {
        this.queryString = queryString;
        this.backendStart = backendStart;
        this.queryStart = queryStart;
        this.exactStart = exactStart;
        this.slowQuery = slowQuery;
    }

    public String getQueryString() {
        return queryString;
    }

    public boolean isSlowQuery() {
        return slowQuery;
    }

    public String getBackendStart() {
        return backendStart;
    }

    public String getQueryStart() {
        return queryStart;
    }

    public String getExactStart() {
        return exactStart;
    }

    @Override
    public String toString() {
        return "Query{" +
                "queryString='" + queryString + '\'' +
                ", slowQuery=" + slowQuery +
                ", backendStart='" + backendStart + '\'' +
                ", queryStart='" + queryStart + '\'' +
                ", exactStart='" + exactStart + '\'' +
                '}';
    }
}