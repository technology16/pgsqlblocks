package ru.taximaxim.treeviewer.utils;

/**
 * необходим для создания колонок
 */
public interface IColumn {
    String getColumnName();

    String getColumnTooltip();

    int getColumnWidth();
}
