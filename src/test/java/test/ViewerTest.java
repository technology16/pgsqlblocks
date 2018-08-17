package test;


import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.treeviewer.MyTreeViewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewerTest {


    public static void main(String[] args) {
        List<TestObject> list = new ArrayList<>();
        List<TestObject> childlist = new ArrayList<>();
        TestObject testObject = new TestObject("test1");
        TestObject testObject2 = new TestObject("test2");
        TestObject testObject3 = new TestObject("test3");
        TestObject childObject = new TestObject("childtest1");
        TestObject childObject2 = new TestObject("childtest2");
        TestObject childObject3 = new TestObject("childtest3");
        childlist.addAll(Arrays.asList(childObject, childObject2, childObject3));
        testObject.setChildren(childlist);
        testObject2.setChildren(childlist);
        testObject3.setChildren(childlist);
        list.addAll(Arrays.asList(testObject, testObject2, testObject3));

        Display display = new Display ();
        Shell shell = new Shell (display);
        shell.setLayout(new FillLayout());
        MyTreeViewer treeViewer = new MyTreeViewer(shell, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI );
        treeViewer.setDataSource(new ExampleDataSource(null));
        treeViewer.setInput(list);
        shell.setSize (400, 400);
        shell.open ();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}
