/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.common.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;

public class DBModelsView extends Composite {

    private final ResourceBundle resourceBundle;

    private static final int COUNT_COLUMN_WIDTH = 50;
    private static final int NAME_COLUMN_WIDTH = 150;

    private TreeViewer treeViewer;

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
        treeViewer = new TreeViewer(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeViewer.getControl().setLayoutData(layoutData);
        treeViewer.getTree().setHeaderVisible(true);
        TreeViewerColumn dbNameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        dbNameColumn.getColumn().setText(resourceBundle.getString("database"));
        dbNameColumn.getColumn().setWidth(NAME_COLUMN_WIDTH);
        dbNameColumn.getColumn().addSelectionListener(getHeaderSelectionAdapter(Columns.NAME));

        TreeViewerColumn processesCountColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        processesCountColumn.getColumn().setText(resourceBundle.getString("processes"));
        processesCountColumn.getColumn().setWidth(COUNT_COLUMN_WIDTH);
        processesCountColumn.getColumn().addSelectionListener(getHeaderSelectionAdapter(Columns.COUNT));

        treeViewer.setContentProvider(new DBModelsViewContentProvider(resourceBundle));
        treeViewer.setLabelProvider(new DBModelsViewLabelProvider(resourceBundle));
        treeViewer.addSelectionChangedListener(event -> {
            if (!event.getSelection().isEmpty()) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object el = selection.getFirstElement();
                if (el instanceof DBController) {
                    DBController selectedController = (DBController) el;
                    listeners.forEach(listener -> listener.dbModelsViewDidSelectController(selectedController));
                } else {
                    listeners.forEach(DBModelsViewListener::dbModelsViewDidSelectGroup);
                }
            }
        });
        treeViewer.addDoubleClickListener(event -> {
            if (!event.getSelection().isEmpty()) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object el = selection.getFirstElement();
                if (el instanceof DBController) {
                    DBController selectedController = (DBController) selection.getFirstElement();
                    listeners.forEach(listener -> listener.dbModelsViewDidCallActionToController(selectedController));
                }
            }
        });

        treeViewer.setComparator(comparator);

        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(treeViewer.getControl());
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(this::menuDidShow);
        treeViewer.getControl().setMenu(menu);
    }

    private SelectionAdapter getHeaderSelectionAdapter(final Columns name) {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeColumn column = (TreeColumn) e.getSource();
                Tree tree = treeViewer.getTree();
                TreeColumn prevSortColumn = tree.getSortColumn();
                boolean desc = true;
                if (prevSortColumn != null) {
                    int prevSortDir = tree.getSortDirection();
                    if (column.equals(prevSortColumn)) {
                        desc = prevSortDir == SWT.UP;
                        tree.setSortDirection(desc ? SWT.DOWN : SWT.UP);
                    } else {
                        tree.setSortColumn(column);
                        tree.setSortDirection(SWT.DOWN);
                    }
                } else {
                    tree.setSortColumn(column);
                    tree.setSortDirection(SWT.DOWN);
                }
                comparator.setColumn(name, desc);
                treeViewer.refresh();
            }
        };
    }

    private void menuDidShow(IMenuManager manager) {
        if (treeViewer.getSelection() instanceof IStructuredSelection) {
            listeners.forEach(listener -> listener.dbModelsViewDidShowMenu(manager));
        }
    }

    public void refresh() {
        treeViewer.refresh();
    }

    public void setInput(List<DBController> dbControllers) {
        treeViewer.setInput(dbControllers);
    }

    public DBController getSelection() {
        Object element = treeViewer.getStructuredSelection().getFirstElement();
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

            int res = 0;
            if (e1 instanceof DBController) {
                DBController el1 = (DBController) e1;
                DBController el2 = (DBController) e2;

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
            } else if (column.col == Columns.NAME) {
                res = e1.toString().compareTo(e2.toString());
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
