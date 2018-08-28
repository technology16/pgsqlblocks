package newview;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;
import ru.taximaxim.treeviewer.models.ObjectViewComparator;


public class TestComparator extends ObjectViewComparator{

    private Columns column;
    private int sortDirection;

    @Override
    public void setColumn(TreeColumn column) {
        this.column = (Columns) column.getData();
    }

    @Override
    public void setSortDirection(int sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int compareResult = 0;
        Test test1 = (Test) e1;
        Test test2 = (Test) e2;
        switch (column){
            case NAME:
               compareResult = compareStringValues(test1.getName(), test2.getName());
               break;
            case PRICE:
                compareResult = compareIntegerValues(test1.getPrice(), test2.getPrice());
                break;
            case TITLE:
                compareResult = compareStringValues(test1.getTitle(), test2.getTitle());
                break;
            case AUTHOR:
                compareResult = compareStringValues(test1.getAuthor(), test2.getAuthor());
                break;
            default:
                break;
        }
        return sortDirection == SWT.DOWN ? compareResult : -compareResult;
    }

    @Override
    public int compareIntegerValues(int one, int two) {
        Integer integer1 = one;
        Integer integer2 = two;
        return integer1.compareTo(integer2);
    }

    @Override
    public int compareStringValues(String one, String two) {
        return one.compareTo(two);
    }

    @Override
    public int compareBooleans(boolean one, boolean two) {
        return one == two ? 0 : one ? 1 : -1;
    }
}
