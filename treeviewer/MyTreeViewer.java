package ru.taximaxim.treeviewer;


import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import ru.taximaxim.treeviewer.utils.MyTreeViewerDataSource;
import ru.taximaxim.treeviewer.utils.IColumn;

/**
 * Основной класс, который инициализируется в UI
 */
public class MyTreeViewer extends TreeViewer {

    private MyTreeViewerDataSource dataSource;

    public MyTreeViewer(Composite parent, int style) {
        super(parent, style);
        getTree().setLinesVisible(true);
        getTree().setHeaderVisible(true);
    }

    public MyTreeViewerDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(MyTreeViewerDataSource dataSource) {
        if (this.dataSource != null) {
            throw new IllegalStateException("MyTreeViewer already contains data source");
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
}
