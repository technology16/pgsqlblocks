package ru.taximaxim.treeviewer.tree;


import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import ru.taximaxim.treeviewer.filter.FilterValues;
import ru.taximaxim.treeviewer.filter.MyTreeViewerFilter;
import ru.taximaxim.treeviewer.listeners.FilterListener;
import ru.taximaxim.treeviewer.listeners.MyTreeViewerSortColumnSelectionListener;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;
import ru.taximaxim.treeviewer.models.IColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Основной класс, который инициализируется в UI
 */
public class MyTreeViewerTable extends TreeViewer{

    private MyTreeViewerDataSource dataSource;
    private Set<IColumn> invisibleColumns;
    private List<MyTreeViewerSortColumnSelectionListener> sortColumnlisteners = new ArrayList<>();

    public MyTreeViewerTable(Composite parent, int style) {
        super(parent, style);
        getTree().setLinesVisible(true);
        getTree().setHeaderVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        getTree().setLayoutData(data);
    }

    public MyTreeViewerDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(MyTreeViewerDataSource dataSource) {
        if (this.dataSource != null) {
            throw new IllegalStateException("MyTreeViewerTable already contains data source");
        }
        this.dataSource = dataSource;
        createColumns();
        setLabelProvider(this.dataSource);
        setContentProvider(this.dataSource);
    }

    private void createColumns() {
        for (IColumn column : dataSource.getColumns()) {
            TreeViewerColumn treeColumn = new TreeViewerColumn(this, SWT.NONE);
            treeColumn.getColumn().setText(dataSource.getLocalizeString(column.getColumnName()));
            treeColumn.getColumn().setMoveable(true);
            treeColumn.getColumn().setToolTipText(column.getColumnTooltip());
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

    public void addSortListener(MyTreeViewerSortColumnSelectionListener listener){
        sortColumnlisteners.add(listener);
    }

    public void removeSortListener(MyTreeViewerSortColumnSelectionListener listener){
        sortColumnlisteners.remove(listener);
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
        sortColumnlisteners.forEach(listener -> listener.didSelectSortColumn(column, fSortDirection));
    }


    public Set<IColumn> getInvisibleColumns() {
        return invisibleColumns;
    }

    /**
     * Список приходит из диалога конфигурации списка колонок
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
}
