package ru.taximaxim.treeviewer.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.tree.ExtendedTreeViewerComponent;
import ru.taximaxim.treeviewer.utils.ColumnType;

import java.util.*;

public class FilterChangeHandler {

    private DataSource<? extends IObject> dataSource;
    private ExtendedTreeViewerComponent tree;
    private Map<IColumn, ViewerFilter> columnFilters = new HashMap<>();
    private ViewerFilter allTextFilter;

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
                    return dataSource.getColumnsToFilter().stream()
                            .anyMatch(column -> {
                                String textFromObject = dataSource.getRowText(element, column);
                                return FilterOperation.CONTAINS.matchesForType(textFromObject, searchText, ColumnType.STRING);
                            });
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
                    String textFromObject = dataSource.getRowText(element, column);
                    return value.matchesForType(textFromObject, searchText, column.getColumnType());
                }
            };
            columnFilters.put(column, columnFilter);
        }
        updateFilters();
    }

    private void updateFilters() {
        List<ViewerFilter> filters = new ArrayList<>(columnFilters.values());
        if (allTextFilter != null) {
            filters.add(allTextFilter);
        }
        tree.setFilters(filters.toArray(new ViewerFilter[0]));
    }
}
