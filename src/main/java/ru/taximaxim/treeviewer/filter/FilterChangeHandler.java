/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2022 "Technology" LLC
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
package ru.taximaxim.treeviewer.filter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.tree.ExtendedTreeViewerComponent;

public class FilterChangeHandler {

    private final DataSource<? extends IObject> dataSource;
    private ExtendedTreeViewerComponent<?> tree;
    private final Map<Columns, ViewerFilter> columnFilters = new EnumMap<>(Columns.class);
    private ViewerFilter allTextFilter;

    public FilterChangeHandler(DataSource<? extends IObject> dataSource) {
        this.dataSource = dataSource;
    }

    public void setTree(ExtendedTreeViewerComponent<?> tree) {
        this.tree = tree;
    }

    void filterAllColumns(String searchText) {
        if (searchText.isEmpty()) {
            allTextFilter = null;
        } else {
            allTextFilter = new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    if (parentElement instanceof IObject) {
                        // Show all children (not first-level elements) because all children
                        // should be shown if parent is shown. If parent is not matched then
                        // his children will not be shown anyway.
                        return true;
                    } else {
                        return dataSource.getColumnsToFilter().stream()
                                .anyMatch(column -> matches(column, (IObject)element, searchText,
                                        FilterOperation.CONTAINS));
                    }
                }
            };
        }
        updateFilters();
    }

    void filter(String searchText, FilterOperation value, Columns column) {
        if (searchText.isEmpty() || value == FilterOperation.NONE) {
            columnFilters.remove(column);
        } else {
            ViewerFilter columnFilter = new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    if (parentElement instanceof IObject) {
                        // Show all children (not first-level elements) because all children
                        // should be shown if parent is shown. If parent is not matched then
                        // his children will not be shown anyway.
                        return true;
                    } else {
                        return matches(column, (IObject) element, searchText, value);
                    }
                }
            };
            columnFilters.put(column, columnFilter);
        }
        updateFilters();
    }

    void deactivateFilters() {
        allTextFilter = null;
        columnFilters.clear();
        updateFilters();
    }

    private boolean matches(Columns column, IObject element, String searchText,
            FilterOperation value) {
        if (element.hasChildren()) {
            for (IObject child : element.getChildren()) {
                if (matches(column, child, searchText, value)) {
                    return true;
                }
            }
        }
        String textFromObject = dataSource.getRowText(element, column);
        return value.matchesForType(textFromObject, searchText, getColumnType(column));
    }

    private ColumnType getColumnType(Columns column) {
        switch (column) {
        case PID: return ColumnType.INTEGER;
        case BLOCK_CREATE_DATE:
        case BLOCK_END_DATE:
        case BACKEND_START:
        case QUERY_START:
        case XACT_START:
        case DURATION:
        case STATE_CHANGE: return ColumnType.DATE;
        default: return ColumnType.STRING;
        }
    }

    private void updateFilters() {
        List<ViewerFilter> filters = new ArrayList<>(columnFilters.values());
        if (allTextFilter != null) {
            filters.add(allTextFilter);
        }
        tree.setFilters(filters.toArray(new ViewerFilter[0]));
    }
}
