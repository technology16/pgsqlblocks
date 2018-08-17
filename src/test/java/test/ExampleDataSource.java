package test;

import org.eclipse.swt.graphics.Image;
import ru.taximaxim.treeviewer.utils.IColumn;
import ru.taximaxim.treeviewer.utils.MyTreeViewerDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by user on 16.08.18.
 */
public class ExampleDataSource extends MyTreeViewerDataSource{

    protected final ResourceBundle resourceBundle;


    public ExampleDataSource(ResourceBundle resourceBundle) {
        super();
        this.resourceBundle = resourceBundle;
    }

    @Override
    public boolean columnIsSortable() {
        return false;
    }

    @Override
    public List<IColumn> getColumns() {
        List<IColumn> list = new ArrayList<>();
        list.add(new Col("title", "tool", 80));
        list.add(new Col("namee", "nametool", 80));
        return list;
    }


    @Override
    public String getLocalizeString(String name) {
        if (resourceBundle != null) {
            return resourceBundle.getString(name);
        }
        return name;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        return getRowText(element, getColumns().get(columnIndex));
    }

    private String getRowText(Object element, IColumn iColumn) {
        TestObject test = (TestObject) element;
        switch (iColumn.getColumnName()){
            case "title":
                return test.getName();
        }
        return "TEXT";
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<TestObject> list = (List<TestObject>) inputElement;
        return list.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        TestObject testObject = (TestObject)parentElement;
        return testObject.getChildren().toArray();
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        TestObject testObject = (TestObject) element;
        return testObject.hasChildren();
    }


}
