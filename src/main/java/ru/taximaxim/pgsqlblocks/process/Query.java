package ru.taximaxim.pgsqlblocks.process;

public class Query {
    private final String query;
    private final boolean slowQuery;
    private final String backendStart;
    private final String queryStart;
    private final String exactStart;

    Query(String query, String backendStart, String queryStart, String exactStart, boolean slowQuery) {
        this.query = query;
        this.backendStart = backendStart;
        this.queryStart = queryStart;
        this.exactStart = exactStart;
        this.slowQuery = slowQuery;
    }

    public String getQuery() {
        return query;
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
                "query='" + query + '\'' +
                ", slowQuery=" + slowQuery +
                ", backendStart='" + backendStart + '\'' +
                ", queryStart='" + queryStart + '\'' +
                ", exactStart='" + exactStart + '\'' +
                '}';
    }
}