package ru.taximaxim.treeviewer.listeners;


import ru.taximaxim.treeviewer.filter.ViewFilter;

/**
 * Listener for one column
 */
public interface FilterListener {

    void filter(ViewFilter filter);
}
