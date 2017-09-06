package ru.taximaxim.pgsqlblocks.common.models;

import java.time.Duration;
import java.util.Date;

public class DBProcessQuery {

    private final String queryString;
    private final boolean slowQuery;
    private final Date backendStart;
    private final Date queryStart;
    private final Date xactStart;

    public DBProcessQuery(String queryString, boolean slowQuery, Date backendStart, Date queryStart, Date xactStart) {
        this.queryString = queryString == null ? "" : queryString;
        this.slowQuery = slowQuery;
        this.backendStart = backendStart;
        this.queryStart = queryStart;
        this.xactStart = xactStart;
    }

    public String getQueryString() {
        return queryString;
    }

    public boolean isSlowQuery() {
        return slowQuery;
    }

    public Date getBackendStart() {
        return backendStart;
    }

    public Date getQueryStart() {
        return queryStart;
    }

    public Date getXactStart() {
        return xactStart;
    }

    public Duration getDuration() {
        return xactStart == null ? null : Duration.ofMillis(new Date().getTime() - xactStart.getTime());
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
        if (backendStart != null ? !backendStart.equals(that.backendStart) : that.backendStart != null) return false;
        if (queryStart != null ? !queryStart.equals(that.queryStart) : that.queryStart != null) return false;
        return xactStart != null ? xactStart.equals(that.xactStart) : that.xactStart == null;
    }

    @Override
    public int hashCode() {
        int result = queryString.hashCode();
        result = 31 * result + (slowQuery ? 1 : 0);
        result = 31 * result + (backendStart != null ? backendStart.hashCode() : 0);
        result = 31 * result + (queryStart != null ? queryStart.hashCode() : 0);
        result = 31 * result + (xactStart != null ? xactStart.hashCode() : 0);
        return result;
    }
}
