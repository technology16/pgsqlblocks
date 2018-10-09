package ru.taximaxim.treeviewer.models;

import ru.taximaxim.treeviewer.utils.ColumnType;

/**
 * Methods which need to implement in column class
 */
public interface IColumn {

    ColumnType getColumnType();

    String getColumnName();

    String getColumnTooltip();

    int getColumnWidth();
}
