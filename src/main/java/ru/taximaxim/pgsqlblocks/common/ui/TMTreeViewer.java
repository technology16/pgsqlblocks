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

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import ru.taximaxim.pgsqlblocks.utils.Columns;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TMTreeViewer extends TreeViewer {

    private TMTreeViewerDataSource dataSource;

    private List<TMTreeViewerSortColumnSelectionListener> sortColumnSelectionListeners = new ArrayList<>();
    private Set<Integer> collapsedColumnsIndexes;

    public TMTreeViewer(Composite parent) {
        super(parent);
    }

    public TMTreeViewer(Composite parent, int style) {
        super(parent, style);
        getTree().setLinesVisible(true);
        getTree().setHeaderVisible(true);
    }

    public TMTreeViewer(Tree tree) {
        super(tree);
    }

    public TMTreeViewerDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(TMTreeViewerDataSource dataSource) {
        if (this.dataSource != null) {
            throw new IllegalStateException("TMTreeViewer already contains data source");
        }
        this.dataSource = dataSource;
        createColumns();
        setLabelProvider(this.dataSource);
        setContentProvider(this.dataSource);
        addDoubleClickListener(new DoubleClickListener());
    }

    private void selectSortColumn(TreeColumn column, int columnIndex) {
        TreeColumn prevSortColumn = getTree().getSortColumn();
        int sortDirection = SWT.DOWN;
        if (prevSortColumn != null) {
            int prevSortDirection = getTree().getSortDirection();
            if (column.equals(prevSortColumn)) {
                sortDirection = prevSortDirection == SWT.UP ? SWT.DOWN : SWT.UP;
                getTree().setSortDirection(sortDirection);
            } else {
                getTree().setSortColumn(column);
                getTree().setSortDirection(SWT.DOWN);
            }
        } else {
            getTree().setSortColumn(column);
            getTree().setSortDirection(SWT.DOWN);
        }
        int fSortDirection = sortDirection;
        sortColumnSelectionListeners.forEach(listener -> listener.didSelectSortColumn(column, columnIndex, fSortDirection));
    }

    private void createColumns() {
        for (Columns column : dataSource.getColumns()) {
            TreeViewerColumn treeColumn = new TreeViewerColumn(this, SWT.NONE);
            treeColumn.getColumn().setText(dataSource.getColumnTitle(column.getColumnName()));
            treeColumn.getColumn().setMoveable(true);
            treeColumn.getColumn().setToolTipText(column.getColumnTooltip());
            treeColumn.getColumn().setWidth(column.getColumnWidth());
            treeColumn.getColumn().setData(column);
            if (dataSource.columnIsSortable()) {
                TreeColumn swtColumn = treeColumn.getColumn();

                final int columnIndex = dataSource.getColumns().indexOf(column);
                swtColumn.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        selectSortColumn(swtColumn, columnIndex);
                    }
                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        selectSortColumn(swtColumn, columnIndex);
                    }
                });
            }
        }
    }

    public Set<Integer> getCollapsedColumnsIndexes() {
        return collapsedColumnsIndexes;
    }

    public void setCollapsedColumnsIndexes(Set<Integer> indexes) {
        if (collapsedColumnsIndexes != null) {
            if (indexes != null) {
                collapsedColumnsIndexes.removeAll(indexes);
            }
            expandColumnsAtIndexes(collapsedColumnsIndexes);
        }
        collapsedColumnsIndexes = indexes;
        collapseColumnsAtIndexes(collapsedColumnsIndexes);
    }

    private void expandColumnsAtIndexes(Set<Integer> indexes) {
        if (indexes != null && !indexes.isEmpty()) {
            TreeColumn[] columns = getTree().getColumns();
            for (int i = 0; i < columns.length; i++) {
                if (!indexes.contains(i)) {
                    continue;
                }
                TreeColumn column = columns[i];
                column.setWidth((dataSource.getColumns().get(i)).getColumnWidth());
                column.setResizable(true);
            }
        }
    }

    private void collapseColumnsAtIndexes(Set<Integer> indexes) {
        if (indexes == null || indexes.isEmpty()) {
            return;
        }
        TreeColumn[] columns = getTree().getColumns();
        for (int i = 0; i < columns.length; i++) {
            if (!indexes.contains(i)) {
                continue;
            }
            TreeColumn column = columns[i];
            column.setWidth(0);
            column.setResizable(false);
        }
    }

    public void addSortColumnSelectionListener(TMTreeViewerSortColumnSelectionListener listener) {
        sortColumnSelectionListeners.add(listener);
    }

    public void removeSortColumnSelectionListener(TMTreeViewerSortColumnSelectionListener listener) {
        sortColumnSelectionListeners.remove(listener);
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
}
