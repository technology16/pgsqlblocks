package ru.taximaxim.pgsqlblocks.process;

public class Query {
    private final String queryString;
    private final boolean slowQuery;
    private final String backendStart;
    private final String queryStart;
    private final String exactStart;

    Query(String queryString, String backendStart, String queryStart, String exactStart, boolean slowQuery) {
        this.queryString = queryString == null ? "" : queryString;
        this.backendStart = backendStart == null ? "" : backendStart;
        this.queryStart = queryStart == null ? "" : queryStart;
        this.exactStart = exactStart == null ? "" : exactStart;
        this.slowQuery = slowQuery;
    }

    String getQueryString() {
        return queryString;
    }

    boolean isSlowQuery() {
        return slowQuery;
    }

    public String getBackendStart() {
        return backendStart;
    }

    public String getQueryStart() {
        return queryStart;
    }

    String getExactStart() {
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