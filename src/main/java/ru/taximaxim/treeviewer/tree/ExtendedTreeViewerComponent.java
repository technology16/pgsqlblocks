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
package ru.taximaxim.treeviewer.tree;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.IObject;

/**
 * Class for TreeViewer
 */
public class ExtendedTreeViewerComponent<T extends IObject> extends TreeViewer {

    private DataSource<T> dataSource;
    private Set<IColumn> invisibleColumns;
    private final DBProcessesViewComparator comparator = new DBProcessesViewComparator();

    public ExtendedTreeViewerComponent(Composite parent, int style) {
        super(parent, style);
        getTree().setLinesVisible(true);
        getTree().setHeaderVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        getTree().setLayoutData(data);
        setComparator(comparator);
    }

    public DataSource<T> getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource<T> dataSource) {
        if (this.dataSource != null) {
            throw new IllegalStateException("ExtendedTreeViewerComponent already contains data source");
        }
        this.dataSource = dataSource;
        createColumns();
        setLabelProvider(this.dataSource);
        setContentProvider(this.dataSource);
        addDoubleClickListener(new DoubleClickListener());
    }

    private void createColumns() {
        for (IColumn column : dataSource.getColumns()) {
            TreeViewerColumn treeColumn = new TreeViewerColumn(this, SWT.NONE);
            TreeColumn swtColumn = treeColumn.getColumn();

            swtColumn.setText(dataSource.getLocalizeString(column.getColumnName()));
            swtColumn.setMoveable(true);
            swtColumn.setToolTipText(dataSource.getLocalizeString(column.getColumnTooltip()));
            swtColumn.setWidth(column.getColumnWidth());
            swtColumn.setData(column);
            swtColumn.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    selectSortColumn(swtColumn, (e.stateMask & SWT.CTRL) != 0);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    selectSortColumn(swtColumn, (e.stateMask & SWT.CTRL) != 0);
                }
            });
        }
    }

    private void selectSortColumn(TreeColumn column, boolean isNeedClear) {
        if (isNeedClear) {
            comparator.clearSortList();
            setColumnHeaders();
        }
        comparator.setColumn(column);

        updateSortIndexes();
        refresh();
    }

    private void setColumnHeaders() {
        for (TreeColumn s : getTree().getColumns()) {
            Columns col = (Columns) s.getData();
            s.setText(dataSource.getLocalizeString(col.getColumnName()));
        }
    }

    public Set<IColumn> getInvisibleColumns() {
        return invisibleColumns;
    }

    /**
     * The list comes from the column configuration dialog
     */
    public void setInvisibleColumns(Set<IColumn> invisible) {
        if (invisibleColumns != null) {
            if (invisible != null) {
                invisibleColumns.removeAll(invisible);
            }
            showColumns(invisibleColumns);
        }
        invisibleColumns = invisible;
        hideColumns(invisibleColumns);
    }

    /**
     * set invisibility of columns
     */
    private void hideColumns(Set<IColumn> shownColumns) {
        if (shownColumns == null || shownColumns.isEmpty()) {
            return;
        }
        TreeColumn[] columns = getTree().getColumns();
        for (TreeColumn treeColumn : columns) {
            IColumn column = (IColumn) treeColumn.getData();
            if (!shownColumns.contains(column)) {
                continue;
            }
            treeColumn.setWidth(0);
            treeColumn.setResizable(false);
        }
    }

    /**
     * Set visibility of columns
     */
    private void showColumns(Set<IColumn> shownColumns) {
        if (shownColumns != null && !shownColumns.isEmpty()) {
            TreeColumn[] columns = getTree().getColumns();

            for (TreeColumn treeColumn : columns) {
                IColumn column = (IColumn) treeColumn.getData();
                if (shownColumns.contains(column)) {
                    treeColumn.setWidth(column.getColumnWidth());
                    treeColumn.setResizable(true);
                }
            }
        }
    }

    private class DoubleClickListener implements IDoubleClickListener {
        @Override
        public void doubleClick(final DoubleClickEvent event) {
            TreeItem[] selectedItems = getTree().getSelection();
            if (selectedItems.length != 1) {
                return;
            }
            TreeItem selectedItem = selectedItems[0];
            if (selectedItem.getExpanded()) {
                internalCollapseToLevel(selectedItem, AbstractTreeViewer.ALL_LEVELS);
            } else {
                internalExpandToLevel(selectedItem, 1);
            }
        }
    }

    private void updateSortIndexes() {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (SortingColumn col : comparator.sortOrder) {
            sb.setLength(0);
            sb.append(comparator.sortOrder.size() - i++)
            .append(!col.desc ? '\u25BF' : '\u25B5')
            .append('\t');

            sb.append(dataSource.getLocalizeString(col.col.getColumnName()));

            for (TreeColumn s : getTree().getColumns()) {
                if (col.col == s.getData()) {
                    s.setText(sb.toString());
                }
            }
        }
    }

    private class DBProcessesViewComparator extends ViewerComparator {

        private final Deque<SortingColumn> sortOrder = new LinkedList<>();

        public void clearSortList() {
            sortOrder.clear();
        }

        public void setColumn(TreeColumn column) {
            Columns col = (Columns) column.getData();

            if (!sortOrder.isEmpty() && col.equals(sortOrder.getLast().col)) {
                SortingColumn oldCol = sortOrder.pollLast();
                sortOrder.addLast(new SortingColumn(col, !oldCol.desc));
            } else {
                SortingColumn c = new SortingColumn(col, false);
                sortOrder.remove(c);
                sortOrder.addLast(c);
            }
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            DBProcess process1 = (DBProcess) e1;
            DBProcess process2 = (DBProcess) e2;

            Iterator<SortingColumn> it = sortOrder.descendingIterator();
            while (it.hasNext()) {
                SortingColumn c = it.next();
                int compareResult = 0;

                switch (c.col) {
                case PID:
                    compareResult = Integer.compare(process1.getPid(), process2.getPid());
                    break;
                case BLOCKED_COUNT:
                    compareResult = Integer.compare(process1.getChildren().size(),
                            process2.getChildren().size());
                    break;
                case APPLICATION_NAME:
                    compareResult = compareStringValues(process1.getQueryCaller().getApplicationName(),
                            process2.getQueryCaller().getApplicationName());
                    break;
                case DATABASE_NAME:
                    compareResult = compareStringValues(process1.getQueryCaller().getDatabaseName(),
                            process2.getQueryCaller().getDatabaseName());
                    break;
                case USER_NAME:
                    compareResult = compareStringValues(process1.getQueryCaller().getUserName(),
                            process2.getQueryCaller().getUserName());
                    break;
                case CLIENT:
                    compareResult = compareStringValues(process1.getQueryCaller().getClient(),
                            process2.getQueryCaller().getClient());
                    break;
                case BACKEND_START:
                    compareResult = DateUtils.compareDates(process1.getQuery().getBackendStart(),
                            process2.getQuery().getBackendStart());
                    break;
                case QUERY_START:
                    compareResult = DateUtils.compareDates(process1.getQuery().getQueryStart(),
                            process2.getQuery().getQueryStart());
                    break;
                case XACT_START:
                    compareResult = DateUtils.compareDates(process1.getQuery().getXactStart(),
                            process2.getQuery().getXactStart());
                    break;
                case DURATION:
                    compareResult = compareStringValues(process1.getQuery().getDuration(),
                            process2.getQuery().getDuration());
                    break;
                case STATE:
                    compareResult = compareStringValues(process1.getState(),
                            process2.getState());
                    break;
                case STATE_CHANGE:
                    compareResult = DateUtils.compareDates(process1.getStateChange(),
                            process2.getStateChange());
                    break;
                case BLOCKED:
                    compareResult = compareStringValues(process1.getBlocksPidsString(),
                            process2.getBlocksPidsString());
                    break;
                case LOCK_TYPE:
                    compareResult = compareStringValues(process1.getBlocksLocktypesString(),
                            process2.getBlocksLocktypesString());
                    break;
                case RELATION:
                    compareResult = compareStringValues(process1.getBlocksRelationsString(),
                            process2.getBlocksRelationsString());
                    break;
                case QUERY:
                    compareResult = compareStringValues(process1.getQuery().getQueryString(),
                            process2.getQuery().getQueryString());
                    break;
                case SLOW_QUERY:
                    compareResult = Boolean.compare(process1.getQuery().isSlowQuery(),
                            process2.getQuery().isSlowQuery());
                    break;
                default:
                    break;
                }

                if (compareResult != 0) {
                    if (c.desc) {
                        compareResult = -compareResult;
                    }
                    return compareResult;
                }
            }

            return 0;
        }

        private int compareStringValues(String s1, String s2) {
            return s1.compareTo(s2);
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
