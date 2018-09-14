package ru.taximaxim.treeviewer.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.models.SwtTreeViewerDataSource;
import ru.taximaxim.treeviewer.tree.SwtTreeViewerTable;

import java.util.*;

public class ColumnConfigDialog extends Dialog {

    private SwtTreeViewerTable<? extends IObject> treeViewer;
    private ResourceBundle bundle;
    private final Set<IColumn> invisibleColumn = new HashSet<>();

    public ColumnConfigDialog(ResourceBundle resourceBundle, SwtTreeViewerTable<? extends IObject> tree, Shell parent) {
        super(parent);
        this.treeViewer = tree;
        this.bundle = resourceBundle;
        Set<IColumn> columns = this.treeViewer.getInvisibleColumns();
        if (columns != null) {
            this.invisibleColumn.addAll(columns);
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(bundle.getString("columns"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        layout.marginTop = 10;
        container.setLayout(layout);

        SwtTreeViewerDataSource<? extends IObject> dataSource = treeViewer.getDataSource();

        for (IColumn column : dataSource.getColumns()) {
            Button checkBoxButton = new Button(container, SWT.CHECK);
            checkBoxButton.setText(dataSource.getLocalizeString(column.getColumnName()));
            checkBoxButton.setSelection(!invisibleColumn.contains(column));
            checkBoxButton.addListener(SWT.Selection, event -> {
                if (invisibleColumn.contains(column)) {
                   invisibleColumn.remove(column);
                } else {
                    invisibleColumn.add(column);
                }
            });
        }
        return container;
    }

    @Override
    protected void okPressed() {
        this.treeViewer.setInvisibleColumns(invisibleColumn);
        super.okPressed();
    }
}
