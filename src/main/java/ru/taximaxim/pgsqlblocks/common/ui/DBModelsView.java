/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
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

import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;

public class DBModelsView extends Composite {

    private final ResourceBundle resourceBundle;

    private static final int COUNT_COLUMN_WIDTH = 50;
    private static final int NAME_COLUMN_WIDTH = 150;

    private TableViewerColumn dbNameColumn;
    private TableViewerColumn processesCountColumn;

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
        dbNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        dbNameColumn.getColumn().setWidth(NAME_COLUMN_WIDTH);
        dbNameColumn.getColumn().addSelectionListener(getHeaderSelectionAdapter(Columns.NAME));

        processesCountColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        processesCountColumn.getColumn().setWidth(COUNT_COLUMN_WIDTH);
        processesCountColumn.getColumn().addSelectionListener(getHeaderSelectionAdapter(Columns.COUNT));

        setColumnHeaders();

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

    private SelectionAdapter getHeaderSelectionAdapter(final Columns index) {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if ((e.stateMask & SWT.CTRL) != 0){
                    comparator.clearSortList();
                    setColumnHeaders();
                }
                sortViewer(index);
            }
        };
    }

    private void sortViewer(Columns index) {
        comparator.addSort(index);
        updateSortIndexes();
        tableViewer.refresh();
    }

    private void updateSortIndexes(){
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (SortingColumn col : comparator.sortOrder) {
            sb.setLength(0);
            sb.append(comparator.sortOrder.size() - i++)
            .append(!col.desc ? '\u25BF' : '\u25B5')
            .append('\t');

            switch (col.col) {
            case NAME:
                dbNameColumn.getColumn().setText(sb.append(resourceBundle.getString("database")).toString());
                break;
            case COUNT:
                processesCountColumn.getColumn().setText(sb.append(resourceBundle.getString("processes")).toString());
                break;
            default:
                break;
            }
        }
    }

    private void menuDidShow(IMenuManager manager) {
        if (tableViewer.getSelection() instanceof IStructuredSelection) {
            listeners.forEach(listener -> listener.dbModelsViewDidShowMenu(manager));
        }
    }

    public void refresh() {
        updateSortIndexes();
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

    private void setColumnHeaders(){
        dbNameColumn.getColumn().setText(resourceBundle.getString("database"));
        processesCountColumn.getColumn().setText(resourceBundle.getString("processes"));
    }

    private class TableViewerComparator extends ViewerComparator {

        private final Deque<SortingColumn> sortOrder = new LinkedList<>();

        public void clearSortList() {
            sortOrder.clear();
        }

        public void addSort(Columns column) {
            if (!sortOrder.isEmpty() && column.equals(sortOrder.getLast().col)) {
                SortingColumn oldCol = sortOrder.pollLast();
                sortOrder.addLast(new SortingColumn(column, !oldCol.desc));
            } else {
                SortingColumn c = new SortingColumn(column, false);
                sortOrder.remove(c);
                sortOrder.addLast(c);
            }
        }

        @Override
        public int compare(Viewer v, Object e1, Object e2) {
            DBController el1 = (DBController) e1;
            DBController el2 = (DBController) e2;

            Iterator<SortingColumn> it = sortOrder.descendingIterator();
            while (it.hasNext()) {
                SortingColumn c = it.next();
                int res = 0;
                switch (c.col) {
                case NAME:
                    res = el1.getModelName().compareTo(el2.getModelName());
                    break;
                case COUNT:
                    res = Integer.compare(el1.getProcessesCount(), el2.getProcessesCount());
                    break;
                default:
                    break;
                }
                if (res != 0) {
                    return c.desc ? -res : res;
                }
            }

            return 0;
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
