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

import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.tree.ExtendedTreeViewerComponent;

public class ColumnConfigDialog extends Dialog {

    private final ExtendedTreeViewerComponent<?> tree;
    private final ResourceBundle bundle;
    private final Set<Columns> visibleColumn;

    private CheckboxTableViewer viewer;

    public ColumnConfigDialog(ResourceBundle resourceBundle,
            ExtendedTreeViewerComponent<?> tree, Shell parent) {
        super(parent);
        this.tree = tree;
        this.bundle = resourceBundle;
        this.visibleColumn = tree.getVisibleColumns();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(bundle.getString("columns"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        DataSource<?> dataSource = tree.getDataSource();
        viewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
        viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                Columns column = (Columns) element;
                return dataSource.getLocalizeString(column.getColumnName());
            }
        });

        viewer.setInput(dataSource.getColumns());
        viewer.setCheckedElements(visibleColumn.toArray());

        return container;
    }

    @Override
    protected Point getInitialSize() {
        Point size = super.getInitialSize();
        return new Point(size.x, size.y + 25);
    }

    @Override
    protected void okPressed() {
        visibleColumn.clear();

        for (Object obj : viewer.getCheckedElements()) {
            visibleColumn.add((Columns) obj);
        }

        tree.showColumns();
        super.okPressed();
    }
}
