package ru.taximaxim.treeviewer.tree;


import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;
import ru.taximaxim.treeviewer.models.IColumn;

import java.util.Set;

/**
 * Основной класс, который инициализируется в UI
 */
public class MyTreeViewerTable extends TreeViewer{

    private MyTreeViewerDataSource dataSource;
    private Set<IColumn> invisibleColumns;

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
        }
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
