package ru.taximaxim.treeviewer.models;

/**
 * необходим для создания колонок
 */
public interface IColumn {
    String getColumnName();

    String getColumnTooltip();

    int getColumnWidth();
}
