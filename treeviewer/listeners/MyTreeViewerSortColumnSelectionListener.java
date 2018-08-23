package ru.taximaxim.treeviewer.listeners;


import org.eclipse.swt.widgets.TreeColumn;

public interface MyTreeViewerSortColumnSelectionListener {

    void didSelectSortColumn(TreeColumn column, int sortDirection);
}
