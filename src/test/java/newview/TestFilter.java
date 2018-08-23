package newview;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import ru.taximaxim.treeviewer.filter.AllFilter;
import ru.taximaxim.treeviewer.listeners.AllTextFilterListener;
import test.Test;

/**
 * Created by user on 23.08.18.
 */
public class TestFilter extends AllFilter{
    private String searchText;

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        Test test = (Test) element;
        return test.getName().matches(searchText);
    }

    @Override
    public void onAllTextChanges(String text) {
        if (text != null) {
            searchText = text;
        }
    }
}
