package ru.taximaxim.pgsqlblocks.utils;

public enum Columns {
    PID("pid", "PID", 80),
    CREATE_DATE("block_start_date", "CREATE_DATE", 110),
    //FIXME Check close or change text and tooltip
    CLOSE_DATE("block_change_date", "CLOSE_DATE", 150),
    BLOCKED_COUNT("num_of_blocked_processes", "BLOCKED_COUNT", 70),
    APPLICATION_NAME("application", "APPLICATION_NAME", 100),
    DATABASE_NAME("db_name", "DATABASE_NAME", 110),
    USER_NAME("user_name", "USER_NAME", 110),
    CLIENT("client", "CLIENT", 100),
    BACKEND_START("backend_start", "BACKEND_START", 110),
    QUERY_START("query_start", "QUERY_START", 110),
    XACT_START("xact_start", "XACT_START", 150),
    STATE("state", "STATE", 70),
    STATE_CHANGE("state_change", "STATE_CHANGE", 150),
    BLOCKED("blocked_by", "BLOCKED", 110),
    LOCK_TYPE("lock_type", "LOCK_TYPE", 110),
    RELATION("relation", "RELATION", 130),
    SLOW_QUERY("slow_query", "SLOW_QUERY", 150),
    QUERY("query", "QUERY", 100),
    UNDEFINED("UNDEFINED", "UNDEFINED", 110);

    private String columnName;
    private String columnTooltip;
    private int columnWidth;

    Columns(String columnName, String columnTooltip, int columnWidth) {
        this.columnName = columnName;
        this.columnTooltip = columnTooltip;
        this.columnWidth = columnWidth;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnTooltip() {
        return columnTooltip;
    }

    public int getColumnWidth() {
        return columnWidth;
    }
}
