/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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
package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DBModelsView extends Composite {

    private final ResourceBundle resourceBundle;

    private static final int DATABASE_TABLE_WIDTH = 200;

    private TableViewer tableViewer;

    private List<DBModelsViewListener> listeners = new ArrayList<>();

    public DBModelsView(ResourceBundle resourceBundle, Composite parent, int style) {
        super(parent, style);
        this.resourceBundle = resourceBundle;
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
        tableViewer = new TableViewer(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableViewer.getControl().setLayoutData(layoutData);
        tableViewer.getTable().setHeaderVisible(true);
        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
        column.getColumn().setText(resourceBundle.getString("database"));
        column.getColumn().setWidth(DATABASE_TABLE_WIDTH);
        tableViewer.setContentProvider(new DBModelsViewContentProvider());
        tableViewer.setLabelProvider(new DBModelsViewLabelProvider());
        tableViewer.addSelectionChangedListener(event -> {
            if (!event.getSelection().isEmpty()) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                DBController selectedController = (DBController) selection.getFirstElement();
                listeners.forEach(listener -> listener.dbModelsViewDidSelectController(selectedController));
            }
        });
        tableViewer.addDoubleClickListener(event -> {
            if (!event.getSelection().isEmpty()) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                DBController selectedController = (DBController) selection.getFirstElement();
                listeners.forEach(listener -> listener.dbModelsViewDidCallActionToController(selectedController));
            }
        });

        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewer.getControl());
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(this::menuDidShow);
        tableViewer.getControl().setMenu(menu);

    }

    private void menuDidShow(IMenuManager manager) {
        if (tableViewer.getSelection() instanceof IStructuredSelection) {
            listeners.forEach(listener -> listener.dbModelsViewDidShowMenu(manager));
        }
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }

    public void addListener(DBModelsViewListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBModelsViewListener listener) {
        listeners.remove(listener);
    }
}
