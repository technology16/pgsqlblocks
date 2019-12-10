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

import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import ru.taximaxim.pgsqlblocks.utils.ColumnLayout;
import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.pgsqlblocks.xmlstore.ColumnLayoutsXmlStore;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.utils.AggregatingListener;

/**
 * Class for TreeViewer
 */
public class ExtendedTreeViewerComponent<T extends IObject> extends TreeViewer {

    private DataSource<T> dataSource;
    private ColumnLayoutsXmlStore columnLayoutsStore;

    private final Set<Columns> visibleColumns = EnumSet.noneOf(Columns.class);
    private final EnumMap<Columns, TreeColumn> columns = new EnumMap<>(Columns.class);
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

    public void setData(DataSource<T> dataSource, ColumnLayoutsXmlStore columnLayoutsStore) {
        if (this.dataSource != null) {
            throw new IllegalStateException("ExtendedTreeViewerComponent already contains data source");
        }
        this.dataSource = dataSource;
        this.columnLayoutsStore = columnLayoutsStore;
        createColumns();
        loadColumnsFromStore(columnLayoutsStore.readObjects());
        addListeners();
        setLabelProvider(this.dataSource);
        setContentProvider(this.dataSource);
        addDoubleClickListener(new DoubleClickListener());
    }

    private void loadColumnsFromStore(List<ColumnLayout> layouts) {
        int[] order = IntStream.range(0, columns.size()).toArray();

        // errors protection if columns count will changed
        for (int i = 0; i < layouts.size() && i < columns.size(); i++) {
            ColumnLayout layout = layouts.get(i);
            Columns column = layout.getColumn();
            TreeColumn swtColumn = columns.get(column);
            if (swtColumn != null) {
                // search column index or array
                int columnOrder = getColumnIndex(swtColumn, order);

                if (columnOrder != -1) {
                    int temp = order[columnOrder];

                    order[columnOrder] = order[i];
                    order[i] = temp;

                    Integer width = layout.getWidth();
                    if (width == null) {
                        swtColumn.setWidth(0);
                        swtColumn.setResizable(false);
                        visibleColumns.remove(column);
                    } else {
                        swtColumn.setWidth(width);
                    }
                }
            }
        }

        getTree().setColumnOrder(order);
    }

    private int getColumnIndex(TreeColumn swtColumn, int[] order) {
        int columnOrder = getTree().indexOf(swtColumn);
        for (int i = 0; i < order.length; i++) {
            if (columnOrder == order[i]) {
                return i;
            }
        }

        return -1;
    }

    private void createColumns() {
        for (Columns column : dataSource.getColumns()) {
            TreeColumn swtColumn = createColumn(column);
            swtColumn.setWidth(getColumnWidth(column));
            visibleColumns.add(column);
            columns.put(column, swtColumn);
        }
    }

    private TreeColumn createColumn(Columns column) {
        TreeViewerColumn treeColumn = new TreeViewerColumn(this, SWT.NONE);
        TreeColumn swtColumn = treeColumn.getColumn();

        swtColumn.setData(column);
        swtColumn.setMoveable(true);
        swtColumn.setText(dataSource.getLocalizeString(column.getColumnName()));
        swtColumn.setToolTipText(dataSource.getLocalizeString(getColumnTooltip(column)));
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

        return swtColumn;
    }

    private void addListeners() {
        AggregatingListener al = new AggregatingListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                saveLayouts();
            }

            @Override
            public void controlMoved(ControlEvent e) {
                saveLayouts();
            }
        });

        for (TreeColumn swtColumn : columns.values()) {
            swtColumn.addListener(SWT.Resize | SWT.Move, al);
        }
    }


    private void saveLayouts() {
        List<ColumnLayout> list = new ArrayList<>();

        int[] order = getTree().getColumnOrder();
        TreeColumn[] columns = getTree().getColumns();
        for (int i = 0; i < columns.length; i++) {

            TreeColumn col = columns[order[i]];
            Integer width = null;
            if (col.getResizable()) {
                width = col.getWidth();
            }
            list.add(new ColumnLayout(i, (Columns) col.getData(), width));
        }

        columnLayoutsStore.writeObjects(list);
    }

    private String getColumnTooltip(Columns column) {
        switch(column) {
        case PID:
        case BACKEND_TYPE:
        case APPLICATION_NAME:
        case DATABASE_NAME:
        case USER_NAME:
        case CLIENT:
        case BACKEND_START:
        case QUERY_START:
        case XACT_START:
        case STATE:
        case STATE_CHANGE:
        case BLOCKED:
        case LOCK_TYPE:
        case RELATION:
        case SLOW_QUERY:
        case QUERY: return column.name();
        case DURATION: return "now - XACT_START";
        default : return "";
        }
    }

    private int getColumnWidth(Columns column) {
        switch(column) {
        case PID:
        case BACKEND_TYPE: return 80;
        case BLOCKED_COUNT:
        case STATE:
        case DURATION: return 70;
        case CLIENT:
        case APPLICATION_NAME:
        case QUERY: return 100;
        case RELATION: return 130;
        case XACT_START:
        case STATE_CHANGE:
        case BLOCK_END_DATE:
        case SLOW_QUERY: return 150;
        default: return 110;
        }
    }

    private void selectSortColumn(TreeColumn column, boolean isNeedClear) {
        if (isNeedClear) {
            comparator.clearSortList();
            setColumnHeaders();
        }
        comparator.addSort(column);

        updateSortIndexes();
        refresh();
    }

    public void showColumns() {
        for (Entry<Columns, TreeColumn> entry : columns.entrySet()) {
            Columns column = entry.getKey();
            TreeColumn treeColumn = entry.getValue();
            if (visibleColumns.contains(column)) {
                if (!treeColumn.getResizable()) {
                    treeColumn.setWidth(getColumnWidth(column));
                    treeColumn.setResizable(true);
                }
            } else if (treeColumn.getResizable()) {
                treeColumn.setWidth(0);
                treeColumn.setResizable(false);
            }
        }
        saveLayouts();
    }

    private void setColumnHeaders() {
        for (TreeColumn s : getTree().getColumns()) {
            Columns col = (Columns) s.getData();
            s.setText(dataSource.getLocalizeString(col.getColumnName()));
        }
    }

    public Set<Columns> getVisibleColumns() {
        return visibleColumns;
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

            columns.get(col.col).setText(sb.toString());
        }
    }

    private class DBProcessesViewComparator extends ViewerComparator {

        private final Deque<SortingColumn> sortOrder = new LinkedList<>();

        public void clearSortList() {
            sortOrder.clear();
        }

        public void addSort(TreeColumn column) {
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
            Iterator<SortingColumn> it = sortOrder.descendingIterator();
            while (it.hasNext()) {
                SortingColumn c = it.next();
                int compareResult = dataSource.compare(e1, e2, c.col);

                if (compareResult != 0) {
                    if (c.desc) {
                        compareResult = -compareResult;
                    }
                    return compareResult;
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
