package newview;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import ru.taximaxim.treeviewer.MyTreeViewer;
import ru.taximaxim.treeviewer.listeners.AllTextFilterListener;
import ru.taximaxim.treeviewer.listeners.FilterListener;
import ru.taximaxim.treeviewer.filter.ViewFilter;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * А потом просто превратить его в абстрактный класс и спрятать его внутрь
 */
public class MyFilter implements FilterListener, AllTextFilterListener {
    private final MyTreeViewer treeViewer;
    private MyTreeViewerDataSource dataSource;
    private ViewerFilter allTextFilter;
    private ViewerFilter oneColumnFilter;

    public MyFilter(MyTreeViewerDataSource dataSource, MyTreeViewer treeViewer) {
        this.treeViewer = treeViewer;
        this.dataSource = dataSource;
    }

    @Override
    public void filter(ViewFilter filter) {
        String searchText = filter.getSearchText();
        IColumn column = filter.getColumn();
        if (oneColumnFilter != null) {
            treeViewer.getTree().removeFilter(oneColumnFilter);
        }
        if (!searchText.equals("")) {
            oneColumnFilter = new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    String textFromObject = dataSource.getRowText(element, column);
                    return dataSource.resolveTypeOfComparator(filter.getValue(), textFromObject, searchText);
                }
            };
            treeViewer.getTree().addFilter(oneColumnFilter);
        }
    }

    @Override
    public void filterAllColumn(ViewFilter filter) {
        String searchText = filter.getSearchText();
        if (allTextFilter != null) {
            treeViewer.getTree().removeFilter(allTextFilter);
        }
        if (!searchText.equals("")) {
            allTextFilter = new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    Test test = (Test) element;
                    return test.isForAllFilter(searchText);
                }
            };
            treeViewer.getTree().addFilter(allTextFilter);
        }
    }
}
