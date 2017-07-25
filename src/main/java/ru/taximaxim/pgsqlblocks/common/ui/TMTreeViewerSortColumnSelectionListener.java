package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.widgets.TreeColumn;

public interface TMTreeViewerSortColumnSelectionListener {

    void didSelectSortColumn(TreeColumn column, int columnIndex, int sortDirection);

}
