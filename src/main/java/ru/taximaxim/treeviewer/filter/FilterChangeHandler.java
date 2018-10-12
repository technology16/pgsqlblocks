package ru.taximaxim.treeviewer.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.tree.ExtendedTreeViewerComponent;
import ru.taximaxim.treeviewer.utils.ColumnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterChangeHandler {

    private DataSource<? extends IObject> dataSource;
    private ExtendedTreeViewerComponent tree;
    private Map<IColumn, ViewerFilter> columnFilters = new HashMap<>();
    private ViewerFilter allTextFilter;
    private boolean active;

    public FilterChangeHandler(DataSource<? extends IObject> dataSource) {
        this.dataSource = dataSource;
    }

    public void setTree(ExtendedTreeViewerComponent tree) {
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
                                        FilterOperation.CONTAINS, ColumnType.STRING));
                    }
                }
            };
        }
        updateFilters();
    }

    void filter(String searchText, FilterOperation value, IColumn column) {
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
                        return matches(column, (IObject) element, searchText, value, column.getColumnType());
                    }
                }
            };
            columnFilters.put(column, columnFilter);
        }
        updateFilters();
    }

    private boolean matches(IColumn column, IObject element, String searchText,
                            FilterOperation value, ColumnType columnType) {
        if (element.hasChildren()) {
            for (IObject child : element.getChildren()) {
                if (matches(column, child, searchText, value, columnType)) {
                    return true;
                }
            }
        }
        String textFromObject = dataSource.getRowText(element, column);
        return value.matchesForType(textFromObject, searchText, columnType);
    }

    private void updateFilters() {
        List<ViewerFilter> filters = new ArrayList<>(columnFilters.values());
        if (allTextFilter != null) {
            filters.add(allTextFilter);
        }
        tree.setFilters(filters.toArray(new ViewerFilter[0]));
    }

    public void setActive(boolean actived) {
        this.active = actived;
        if (!active && tree != null) {
            allTextFilter = null;
            columnFilters.clear();
            updateFilters();
        }
    }
}
