package ru.taximaxim.treeviewer.listeners;

import org.eclipse.swt.widgets.TreeColumn;

/**
 * Listener for sorting data in column
 */
public interface TreeViewerSortColumnSelectionListener {

    void didSelectSortColumn(TreeColumn column, int sortDirection);
}
