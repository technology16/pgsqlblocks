package ru.taximaxim.treeviewer.tree;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import ru.taximaxim.treeviewer.listeners.TreeViewerSortColumnSelectionListener;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.models.IColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class for TreeViewer
 */
public class ExtendedTreeViewerComponent<T extends IObject> extends TreeViewer {

    private DataSource<T> dataSource;
    private Set<IColumn> invisibleColumns;
    private List<TreeViewerSortColumnSelectionListener> sortColumnListeners = new ArrayList<>();

    public ExtendedTreeViewerComponent(Composite parent, int style) {
        super(parent, style);
        getTree().setLinesVisible(true);
        getTree().setHeaderVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        getTree().setLayoutData(data);
    }

    public DataSource<T> getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource<T> dataSource) {
        if (this.dataSource != null) {
            throw new IllegalStateException("ExtendedTreeViewerComponent already contains data source");
        }
        this.dataSource = dataSource;
        createColumns();
        setLabelProvider(this.dataSource);
        setContentProvider(this.dataSource);
        addDoubleClickListener(new DoubleClickListener());
    }

    private void createColumns() {
        for (IColumn column : dataSource.getColumns()) {
            TreeViewerColumn treeColumn = new TreeViewerColumn(this, SWT.NONE);
            treeColumn.getColumn().setText(dataSource.getLocalizeString(column.getColumnName()));
            treeColumn.getColumn().setMoveable(true);
            treeColumn.getColumn().setToolTipText(dataSource.getLocalizeString(column.getColumnTooltip()));
            treeColumn.getColumn().setWidth(column.getColumnWidth());
            treeColumn.getColumn().setData(column);
            if (dataSource.columnIsSortable()) {
                TreeColumn swtColumn = treeColumn.getColumn();
                swtColumn.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        selectSortColumn(swtColumn);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        selectSortColumn(swtColumn);
                    }
                });
            }
        }
    }

    public void addSortListener(TreeViewerSortColumnSelectionListener listener){
        sortColumnListeners.add(listener);
    }

    public void removeSortListener(TreeViewerSortColumnSelectionListener listener){
        sortColumnListeners.remove(listener);
    }

    private void selectSortColumn(TreeColumn column) {
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
        sortColumnListeners.forEach(listener -> listener.didSelectSortColumn(column, fSortDirection));
    }

    public Set<IColumn> getInvisibleColumns() {
        return invisibleColumns;
    }

    /**
     * The list comes from the column configuration dialog
     */
    public void setInvisibleColumns(Set<IColumn> invisible) {
        if (invisibleColumns != null) {
            if (invisible != null) {
                invisibleColumns.removeAll(invisible);
            }
            showColumns(invisibleColumns);
        }
        invisibleColumns = invisible;
        hideColumns(invisibleColumns);
    }

    /**
     * set invisibility of columns
     */
    private void hideColumns(Set<IColumn> shownColumns) {
        if (shownColumns == null || shownColumns.isEmpty()) {
            return;
        }
        TreeColumn[] columns = getTree().getColumns();
        for (TreeColumn treeColumn : columns) {
            IColumn column = (IColumn) treeColumn.getData();
            if (!shownColumns.contains(column)) {
                continue;
            }
            treeColumn.setWidth(0);
            treeColumn.setResizable(false);
        }
    }

    /**
     * Set visibility of columns
     */
    private void showColumns(Set<IColumn> shownColumns) {
        if (shownColumns != null && !shownColumns.isEmpty()) {
            TreeColumn[] columns = getTree().getColumns();

            for (TreeColumn treeColumn : columns) {
                IColumn column = (IColumn) treeColumn.getData();
                if (shownColumns.contains(column)) {
                    treeColumn.setWidth(column.getColumnWidth());
                    treeColumn.setResizable(true);
                }
            }
        }
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
