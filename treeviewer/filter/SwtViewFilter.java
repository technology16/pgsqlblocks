package ru.taximaxim.treeviewer.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import ru.taximaxim.treeviewer.listeners.AllTextFilterListener;
import ru.taximaxim.treeviewer.listeners.FilterListener;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;
import ru.taximaxim.treeviewer.tree.SwtTreeViewerTable;

/**
 * Class for realization of filtering process
 */
public class SwtViewFilter implements AllTextFilterListener, FilterListener{

    private MyTreeViewerDataSource dataSource;
    private SwtTreeViewerTable tree;
    private ViewerFilter allTextFilter;
    private ViewerFilter oneColumnFilter;

    public SwtViewFilter(MyTreeViewerDataSource dataSource, SwtTreeViewerTable tree) {
        this.dataSource = dataSource;
        this.tree = tree;
    }

    @Override
    public void filterAllColumn(ViewFilter filter) {
        String searchText = filter.getSearchText();
        if (allTextFilter != null) {
            tree.removeFilter(allTextFilter);
        }
        if (!searchText.equals("")) {
            allTextFilter = new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    IObject test = (IObject) element;
                    return test.isForAllTextFilter(searchText);
                }
            };
            tree.addFilter(allTextFilter);
        }
    }

    @Override
    public void filter(ViewFilter filter) {
        String searchText = filter.getSearchText();
        IColumn column = filter.getColumn();
        if (oneColumnFilter != null) {
            tree.removeFilter(oneColumnFilter);
        }
        if (!searchText.equals("")) {
            oneColumnFilter = new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    String textFromObject = dataSource.getRowText(element, column);
                    return dataSource.resolveTypeOfComparator(filter.getValue(), textFromObject, searchText);
                }
            };
            tree.addFilter(oneColumnFilter);
        }
    }
}
