package newview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.treeviewer.SwtTreeViewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by user on 20.08.18.
 */
public class App {

    public static void main(String[] args) {
        java.util.List<Test> list = new ArrayList<>();
        java.util.List<Test> childlist = new ArrayList<>();
        Test testObject = new Test("test1", "gpqweewrw", "jkr", 500455);
        Test testObject2 = new Test("test2", "BC", "JL", 4001);
        Test testObject3 = new Test("test3", "SH","AKD", 2250);
        Test childObject = new Test("childtest1", "childTitle", "childAuthor", 600);
        Test childObject2 = new Test("childtest2", "childTitle", "childAuthor", 600);
        Test childObject3 = new Test("childtest3", "childTitle", "childAuthor", 600);
        childlist.addAll(Arrays.asList(childObject, childObject2, childObject3));
        testObject.setChildren(childlist);
        testObject2.setChildren(childlist);
        testObject3.setChildren(childlist);
        list.addAll(Arrays.asList(testObject, testObject2, testObject3));

        Display display = new Display ();
        Shell shell = new Shell (display);
        shell.setLayout(new GridLayout());
        shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ResourceBundle bundle = ResourceBundle.getBundle(l10n.AppBundle.class.getName(), new Locale("en"));
        ExampleDataSource dataSource = new ExampleDataSource(null);
        SwtTreeViewer treeViewer = new SwtTreeViewer(shell, SWT.FILL | SWT.BORDER, list, dataSource, bundle);
        treeViewer.getTree().setInput(list);
        treeViewer.setComparator(new TestComparator());
        treeViewer.setColumnsForFilterView(dataSource.getColumns());

        shell.open ();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}
