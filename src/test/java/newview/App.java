package newview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.treeviewer.MyTreeViewer;
import ru.taximaxim.treeviewer.models.IColumn;
import test.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 20.08.18.
 */
public class App {

    public static void main(String[] args) {
        java.util.List<Test> list = new ArrayList<>();
        java.util.List<Test> childlist = new ArrayList<>();
        Test testObject = new Test("test1");
        Test testObject2 = new Test("test2");
        Test testObject3 = new Test("test3");
        Test childObject = new Test("childtest1");
        Test childObject2 = new Test("childtest2");
        Test childObject3 = new Test("childtest3");
        childlist.addAll(Arrays.asList(childObject, childObject2, childObject3));
        testObject.setChildren(childlist);
        testObject2.setChildren(childlist);
        testObject3.setChildren(childlist);
        list.addAll(Arrays.asList(testObject, testObject2, testObject3));



        Display display = new Display ();
        Shell shell = new Shell (display);
        shell.setLayout(new GridLayout());
        shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ExampleDataSource dataSource = new ExampleDataSource(null);
        MyTreeViewer treeViewer = new MyTreeViewer(shell, SWT.FILL | SWT.BORDER, list, dataSource);
        treeViewer.setFilters(dataSource.getColumns());
        treeViewer.setDataUpdateListener(() -> {
            list.add(new Test("update!!!!"));
            treeViewer.getTree().refresh();
            //treeViewer.getTree().setInput(list);
        });
//        treeViewer.setFilterListener(new FilterListener() {
//            @Override
//            public void textChanged(IColumn column, String text) {
//                filterText(list, dataSource, column, text);
//            }
//
//            @Override
//            public void comboChanged(IColumn column, FilterValues value) {
//
//            }
//        });


        shell.open ();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }

    private static void filterText(List<Test> list, ExampleDataSource dataSource, IColumn column, String text) {

        //nb[j ibathjv iehif tltn rhsif ytcgtif
    }

}
