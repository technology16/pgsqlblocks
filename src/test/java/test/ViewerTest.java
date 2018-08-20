package test;


import newview.ExampleDataSource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.treeviewer.tree.MyTreeViewerTable;
import ru.taximaxim.treeviewer.filter.MyTreeViewerFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewerTest {

    public static void main(String[] args) {
        List<Test> list = new ArrayList<>();
        List<Test> childlist = new ArrayList<>();
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
        shell.setLayout(new FillLayout());
        MyTreeViewerTable treeViewer = new MyTreeViewerTable(shell, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI );
        treeViewer.setDataSource(new ExampleDataSource(null));
        treeViewer.setInput(list);
        MyTreeViewerFilter filter = new MyTreeViewerFilter(treeViewer.getDataSource().getColumns(), shell, SWT.TOP);


        shell.setSize (400, 400);
        shell.open ();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}
