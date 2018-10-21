/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2018 "Technology" LLC
 * %
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
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
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.tree.ExtendedTreeViewerComponent;

import java.util.*;

public class ColumnConfigDialog extends Dialog {

    private ExtendedTreeViewerComponent<? extends IObject> treeViewer;
    private ResourceBundle bundle;
    private final Set<IColumn> invisibleColumn = new HashSet<>();

    public ColumnConfigDialog(ResourceBundle resourceBundle, ExtendedTreeViewerComponent<? extends IObject> tree, Shell parent) {
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

        DataSource<? extends IObject> dataSource = treeViewer.getDataSource();

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
