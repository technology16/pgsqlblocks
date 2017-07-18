package ru.taximaxim.pgsqlblocks.common.models;

public class DBProcessQuery {

    private final String queryString;
    private final boolean slowQuery;
    private final String backendStart;
    private final String queryStart;
    private final String xactStart;

    public DBProcessQuery(String queryString, boolean slowQuery, String backendStart, String queryStart, String xactStart) {
        this.queryString = queryString == null ? "" : queryString;
        this.slowQuery = slowQuery;
        this.backendStart = backendStart == null ? "" : backendStart;
        this.queryStart = queryStart == null ? "" : queryStart;
        this.xactStart = xactStart == null ? "" : xactStart;
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

    public String getXactStart() {
        return xactStart;
    }

    @Override
    public String toString() {
        return "DBProcessQuery{" +
                "queryString='" + queryString + '\'' +
                ", slowQuery=" + slowQuery +
                ", backendStart='" + backendStart + '\'' +
                ", queryStart='" + queryStart + '\'' +
                ", xactStart='" + xactStart + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBProcessQuery)) return false;

        DBProcessQuery that = (DBProcessQuery) o;

        if (slowQuery != that.slowQuery) return false;
        if (!queryString.equals(that.queryString)) return false;
        if (!backendStart.equals(that.backendStart)) return false;
        if (!queryStart.equals(that.queryStart)) return false;
        return xactStart.equals(that.xactStart);
    }

    @Override
    public int hashCode() {
        int result = queryString.hashCode();
        result = 31 * result + (slowQuery ? 1 : 0);
        result = 31 * result + backendStart.hashCode();
        result = 31 * result + queryStart.hashCode();
        result = 31 * result + xactStart.hashCode();
        return result;
    }
}
