package ru.taximaxim.treeviewer.listeners;

import ru.taximaxim.treeviewer.filter.FilterValues;
import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Listener for filter
 */
public interface FilterListener {

    void textChanged(IColumn column, String text);
    void comboChanged(IColumn column, FilterValues value);
}
