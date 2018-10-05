package ru.taximaxim.pgsqlblocks.utils;

import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.utils.ColumnType;

public enum Columns implements IColumn {
    PID("pid", "PID", 80, ColumnType.INTEGER),
    BLOCK_CREATE_DATE("block_start_date", "", 110, ColumnType.DATE),
    BLOCK_END_DATE("block_end_date", "", 150, ColumnType.DATE),
    BLOCKED_COUNT("num_of_blocked_processes", "", 70, ColumnType.STRING),
    APPLICATION_NAME("application", "APPLICATION_NAME", 100, ColumnType.STRING),
    DATABASE_NAME("db_name", "DATABASE_NAME", 110, ColumnType.STRING),
    USER_NAME("user_name", "USER_NAME", 110, ColumnType.STRING),
    CLIENT("client", "CLIENT", 100, ColumnType.STRING),
    BACKEND_START("backend_start", "BACKEND_START", 110, ColumnType.DATE),
    QUERY_START("query_start", "QUERY_START", 110, ColumnType.DATE),
    XACT_START("xact_start", "XACT_START", 150, ColumnType.DATE),
    DURATION("duration", "now - XACT_START", 70, ColumnType.DATE),
    STATE("state", "STATE", 70, ColumnType.STRING),
    STATE_CHANGE("state_change", "STATE_CHANGE", 150, ColumnType.STRING),
    BLOCKED("blocked_by", "BLOCKED", 110, ColumnType.STRING),
    LOCK_TYPE("lock_type", "LOCK_TYPE", 110, ColumnType.STRING),
    RELATION("relation", "RELATION", 130, ColumnType.STRING),
    SLOW_QUERY("slow_query", "SLOW_QUERY", 150, ColumnType.STRING),
    QUERY("query", "QUERY", 100, ColumnType.STRING);

    private final String columnName;
    private final String columnTooltip;
    private final int columnWidth;
    private final ColumnType columnType;

    Columns(String columnName, String columnTooltip, int columnWidth, ColumnType columnType) {
        this.columnName = columnName;
        this.columnTooltip = columnTooltip;
        this.columnWidth = columnWidth;
        this.columnType = columnType;
    }

    @Override
    public ColumnType getColumnType() {
        return columnType;
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

    public static Columns getColumn(IColumn column) {
        for (Columns columns : Columns.values()) {
            if (columns.getColumnName().equals(column.getColumnName())) {
                return columns;
            }
        }
        return Columns.PID;
    }
}
