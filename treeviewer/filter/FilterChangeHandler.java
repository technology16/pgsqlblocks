package ru.taximaxim.treeviewer.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.models.SwtTreeViewerDataSource;
import ru.taximaxim.treeviewer.tree.SwtTreeViewerTable;

import java.util.*;

public class FilterChangeHandler {

    private SwtTreeViewerDataSource<? extends IObject> dataSource;
    private SwtTreeViewerTable tree;
    private Map<IColumn, ViewerFilter> columnFilters = new HashMap<>();
    private ViewerFilter allTextFilter;

    public FilterChangeHandler(SwtTreeViewerDataSource<? extends IObject> dataSource, SwtTreeViewerTable tree) {
        this.dataSource = dataSource;
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
                                return FilterValues.CONTAINS.test(textFromObject, searchText);
                            });
                }
            };
        }
        updateFilters();
    }

    void filter(String searchText, FilterValues value, IColumn column) {
        if (searchText.isEmpty() || value == FilterValues.NONE) {
            columnFilters.remove(column);
        } else {
            ViewerFilter columnFilter = new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    String textFromObject = dataSource.getRowText(element, column);
                    return value.test(textFromObject, searchText);
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
