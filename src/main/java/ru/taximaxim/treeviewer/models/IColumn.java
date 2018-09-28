package ru.taximaxim.treeviewer.models;

/**
 * Methods which need to implement in column class
 */
public interface IColumn {

    String getColumnName();

    String getColumnTooltip();

    int getColumnWidth();
}
