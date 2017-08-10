package ru.taximaxim.pgsqlblocks.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.pgsqlblocks.common.ui.TMTreeViewer;
import ru.taximaxim.pgsqlblocks.common.ui.TMTreeViewerDataSource;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class TMTreeViewerColumnsDialog extends Dialog {

    private TMTreeViewer treeViewer;
    private ResourceBundle resourceBundle;

    private final Set<Integer> collapsedColumnsIndexes = new HashSet<>();

    public TMTreeViewerColumnsDialog(ResourceBundle resourceBundle, TMTreeViewer treeViewer, Shell shell) {
        super(shell);
        this.resourceBundle = resourceBundle;
        this.treeViewer = treeViewer;
        Set<Integer> collapsedColumnsIndexes = this.treeViewer.getCollapsedColumnsIndexes();
        if (collapsedColumnsIndexes != null) {
            this.collapsedColumnsIndexes.addAll(collapsedColumnsIndexes);
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(resourceBundle.getString("columns"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        layout.marginTop = 10;
        container.setLayout(layout);

        TMTreeViewerDataSource dataSource = treeViewer.getDataSource();

        int columnsCount = dataSource.numberOfColumns();
        for (int i = 0; i < columnsCount; i++) {
            final int columnIndex = i;
            Button checkBoxButton = new Button(container, SWT.CHECK);
            checkBoxButton.setText(dataSource.columnTitleForColumnIndex(columnIndex));
            checkBoxButton.setSelection(!collapsedColumnsIndexes.contains(columnIndex));
            checkBoxButton.addListener(SWT.Selection, event -> {
                if (collapsedColumnsIndexes.contains(columnIndex)) {
                    collapsedColumnsIndexes.remove(columnIndex);
                } else {
                    collapsedColumnsIndexes.add(columnIndex);
                }
            });
        }

        return container;
    }

    @Override
    protected void okPressed() {
        this.treeViewer.setCollapsedColumnsIndexes(collapsedColumnsIndexes);
        super.okPressed();
    }
}
