package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TMTreeViewer extends TreeViewer {

    private TMTreeViewerDataSource dataSource;

    private List<TMTreeViewerSortColumnSelectionListener> sortColumnSelectionListeners = new ArrayList<>();

    public TMTreeViewer(Composite parent) {
        super(parent);
    }

    public TMTreeViewer(Composite parent, int style) {
        super(parent, style);
        getTree().setLinesVisible(true);
        getTree().setHeaderVisible(true);
    }

    public TMTreeViewer(Tree tree) {
        super(tree);
    }

    public TMTreeViewerDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(TMTreeViewerDataSource dataSource) {
        if (this.dataSource != null) {
            throw new IllegalStateException("TMTreeViewer already contains data source");
        }
        this.dataSource = dataSource;
        createColumns();
        setLabelProvider(this.dataSource);
        setContentProvider(this.dataSource);
        addDoubleClickListener(new DoubleClickListener());
    }

    private void selectSortColumn(TreeColumn column, int columnIndex) {
        TreeColumn prevSortColumn = getTree().getSortColumn();
        int sortDirection = SWT.DOWN;
        if (prevSortColumn != null) {
            int prevSortDirection = getTree().getSortDirection();
            if (column.equals(prevSortColumn)) {
                sortDirection = prevSortDirection == SWT.UP ? SWT.DOWN : SWT.UP;
                getTree().setSortDirection(sortDirection);
            } else {
                getTree().setSortColumn(column);
                getTree().setSortDirection(SWT.DOWN);
            }
        } else {
            getTree().setSortColumn(column);
            getTree().setSortDirection(SWT.DOWN);
        }
        int fSortDirection = sortDirection;
        sortColumnSelectionListeners.forEach(listener -> listener.didSelectSortColumn(column, columnIndex, fSortDirection));
    }

    private void createColumns() {
        for (int i = 0; i < dataSource.numberOfColumns(); i++) {
            TreeViewerColumn treeColumn = new TreeViewerColumn(this, SWT.NONE);
            treeColumn.getColumn().setText(dataSource.columnTitleForColumnIndex(i));
            treeColumn.getColumn().setMoveable(true);
            treeColumn.getColumn().setToolTipText(dataSource.columnTooltipForColumnIndex(i));
            treeColumn.getColumn().setWidth(dataSource.columnWidthForColumnIndex(i));
            if (dataSource.columnIsSortableAtIndex(i)) {
                TreeColumn swtColumn = treeColumn.getColumn();
                final int columnIndex = i;
                swtColumn.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        selectSortColumn(swtColumn, columnIndex);
                    }
                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {

                    }
                });
            }
        }
    }

    private Set<Integer> collapsedColumnsIndexes;

    public Set<Integer> getCollapsedColumnsIndexes() {
        return collapsedColumnsIndexes;
    }

    public void setCollapsedColumnsIndexes(Set<Integer> indexes) {
        if (collapsedColumnsIndexes != null) {
            if (indexes != null) {
                collapsedColumnsIndexes.removeAll(indexes);
            }
            expandColumnsAtIndexes(collapsedColumnsIndexes);
        }
        collapsedColumnsIndexes = indexes;
        collapseColumnsAtIndexes(collapsedColumnsIndexes);
    }

    private void expandColumnsAtIndexes(Set<Integer> indexes) {
        if (indexes == null || indexes.isEmpty()) {
            return;
        }
        TreeColumn[] columns = getTree().getColumns();
        if (indexes != null) {
            for (int i = 0; i < columns.length; i++) {
                if (!indexes.contains(i)) {
                    continue;
                }
                TreeColumn column = columns[i];
                column.setWidth(dataSource.columnWidthForColumnIndex(i));
                column.setResizable(true);
            }
        }
    }

    private void collapseColumnsAtIndexes(Set<Integer> indexes) {
        if (indexes == null || indexes.isEmpty()) {
            return;
        }
        TreeColumn[] columns = getTree().getColumns();
        for (int i = 0; i < columns.length; i++) {
            if (!indexes.contains(i)) {
                continue;
            }
            TreeColumn column = columns[i];
            column.setWidth(0);
            column.setResizable(false);
        }
    }

    public void addSortColumnSelectionListener(TMTreeViewerSortColumnSelectionListener listener) {
        sortColumnSelectionListeners.add(listener);
    }

    public void removeSortColumnSelectionListener(TMTreeViewerSortColumnSelectionListener listener) {
        sortColumnSelectionListeners.remove(listener);
    }

    private class DoubleClickListener implements IDoubleClickListener {
        @Override
        public void doubleClick(final DoubleClickEvent event) {
            TreeItem[] selectedItems = getTree().getSelection();
            if (selectedItems.length != 1) {
                return;
            }
            TreeItem selectedItem = selectedItems[0];
            if (selectedItem.getExpanded()) {
                internalCollapseToLevel(selectedItem, AbstractTreeViewer.ALL_LEVELS);
            } else {
                internalExpandToLevel(selectedItem, 1);
            }
        }
    }
}
