package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

public class TMTreeViewer extends TreeViewer {

    private TMTreeViewerDataSource dataSource;

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
    }

    private void createColumns() {
        for (int i = 0; i < dataSource.numberOfColumns(); i++) {
            TreeViewerColumn treeColumn = new TreeViewerColumn(this, SWT.NONE);
            treeColumn.getColumn().setText(dataSource.columnTitleForColumnIndex(i));
            treeColumn.getColumn().setMoveable(true);
            treeColumn.getColumn().setToolTipText(dataSource.columnTooltipForColumnIndex(i));
            treeColumn.getColumn().setWidth(dataSource.columnWidthForColumnIndex(i));
        }
    }


}
