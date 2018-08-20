package ru.taximaxim.treeviewer.tree;


import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import ru.taximaxim.treeviewer.filter.MyTreeViewerFilter;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;
import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Основной класс, который инициализируется в UI
 */
public class MyTreeViewerTable extends TreeViewer {

    private MyTreeViewerDataSource dataSource;
    private MyTreeViewerFilter filter;

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

    public void setFilter(MyTreeViewerFilter filter){
        this.filter = filter;

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
