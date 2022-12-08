/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017-2022 "Technology" LLC
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
package ru.taximaxim.pgsqlblocks.common.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;

public class DBModelsView extends Composite {

    private final ResourceBundle resourceBundle;

    private static final int COUNT_COLUMN_WIDTH = 50;
    private static final int NAME_COLUMN_WIDTH = 150;

    private TableViewer tableViewer;

    private final List<DBModelsViewListener> listeners = new ArrayList<>();
    private final TableViewerComparator comparator = new TableViewerComparator();

    private enum Columns {
        NAME, COUNT
    }

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
        TableViewerColumn dbNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        dbNameColumn.getColumn().setText(resourceBundle.getString("database"));
        dbNameColumn.getColumn().setWidth(NAME_COLUMN_WIDTH);
        dbNameColumn.getColumn().addSelectionListener(getHeaderSelectionAdapter(Columns.NAME));

        TableViewerColumn processesCountColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        processesCountColumn.getColumn().setText(resourceBundle.getString("processes"));
        processesCountColumn.getColumn().setWidth(COUNT_COLUMN_WIDTH);
        processesCountColumn.getColumn().addSelectionListener(getHeaderSelectionAdapter(Columns.COUNT));

        tableViewer.setContentProvider(new DBModelsViewContentProvider());
        tableViewer.setLabelProvider(new DBModelsViewLabelProvider(resourceBundle));
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

        tableViewer.setComparator(comparator);

        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewer.getControl());
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(this::menuDidShow);
        tableViewer.getControl().setMenu(menu);

    }

    private SelectionAdapter getHeaderSelectionAdapter(final Columns name) {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableColumn column = (TableColumn) e.getSource();
                Table table = tableViewer.getTable();
                TableColumn prevSortColumn = table.getSortColumn();
                boolean desc = true;
                if (prevSortColumn != null) {
                    int prevSortDir = table.getSortDirection();
                    if (column.equals(prevSortColumn)) {
                        desc = prevSortDir == SWT.UP;
                        table.setSortDirection(desc ? SWT.DOWN : SWT.UP);
                    } else {
                        table.setSortColumn(column);
                        table.setSortDirection(SWT.DOWN);
                    }
                } else {
                    table.setSortColumn(column);
                    table.setSortDirection(SWT.DOWN);
                }

                comparator.setColumn(name, desc);
                tableViewer.refresh();
            }
        };
    }

    private void menuDidShow(IMenuManager manager) {
        if (tableViewer.getSelection() instanceof IStructuredSelection) {
            listeners.forEach(listener -> listener.dbModelsViewDidShowMenu(manager));
        }
    }

    public void refresh() {
        tableViewer.refresh();
    }

    public void setInput(List<DBController> dbControllers) {
        tableViewer.setInput(dbControllers);
    }

    public DBController getSelection() {
        Object element = tableViewer.getStructuredSelection().getFirstElement();
        if (element instanceof DBController) {
            return (DBController) element;
        }

        return null;
    }

    public void addListener(DBModelsViewListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBModelsViewListener listener) {
        listeners.remove(listener);
    }

    private class TableViewerComparator extends ViewerComparator {

        private SortingColumn column;

        public void setColumn(Columns col, boolean desc) {
            this.column = new SortingColumn(col, desc);
        }

        @Override
        public int compare(Viewer v, Object e1, Object e2) {
            if (column == null) {
                return 0;
            }

            DBController el1 = (DBController) e1;
            DBController el2 = (DBController) e2;

            int res = 0;
            switch (column.col) {
            case NAME:
                res = el1.getModelName().compareTo(el2.getModelName());
                break;
            case COUNT:
                res = Integer.compare(el1.getProcessesCount(), el2.getProcessesCount());
                break;
            default:
                break;
            }

            return column.desc ? -res : res;
        }
    }

    private static class SortingColumn {

        private final Columns col;
        private final boolean desc;

        public SortingColumn(Columns col, boolean desc) {
            this.col = col;
            this.desc = desc;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SortingColumn
                    && ((SortingColumn) obj).col == col;
        }

        @Override
        public int hashCode() {
            return col.hashCode();
        }
    }
}
