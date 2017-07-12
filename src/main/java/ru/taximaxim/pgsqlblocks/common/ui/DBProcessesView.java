package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class DBProcessesView extends Composite {

    private TreeViewer treeViewer;

    public DBProcessesView(Composite parent, int style) {
        super(parent, style);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
        treeViewer = new TreeViewer(this, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeViewer.getControl().setLayoutData(layoutData);
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }
}
