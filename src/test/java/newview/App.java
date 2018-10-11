package newview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.treeviewer.ExtendedTreeViewer;

import java.util.*;

/**
 * Created by user on 20.08.18.
 */
public class App {

    public static void main(String[] args) {
        List<Test> testList = prepareList();

        Display display = new Display ();
        Shell shell = new Shell (display);
        shell.setLayout(new GridLayout());
        shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ExampleDataSource dataSource = new ExampleDataSource(null);
        ExtendedTreeViewer<Test> treeViewer = new ExtendedTreeViewer<Test>(shell, SWT.FILL | SWT.BORDER, testList, dataSource, new Locale("en"));
        treeViewer.setComparator(new TestComparator());

        shell.open ();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }

    private static List<Test> prepareList() {
        List<Test> childlist = new ArrayList<>();
        List<Test> childlist2 = new ArrayList<>();
        List<Test> childlist3 = new ArrayList<>();

        Test testObject = new Test("test1", "gpqweewrw", "jkr", 500455);
        Test testObject2 = new Test("test2", "BC", "JL", 4001);
        Test testObject3 = new Test("test3", "SH","AKD", 2250);

        Test childObject1 = new Test("childtest1", "childTitle", "childAuthor", 600);
        Test childObject2 = new Test("childtest2", "childTitle", "childAuthor", 200);
        Test childObject3 = new Test("childtest3", "childTitle", "childAuthor", 400);

        Test childObject21 = new Test("childtest21", "childTitle", "childAuthor", 500);
        Test childObject22 = new Test("childtest22", "childTitle", "childAuthor", 250);
        Test childObject23 = new Test("childtest23", "childTitle", "childAuthor", 470);

        Test childObject31 = new Test("childtest31", "childTitle", "childAuthor", 300);
        Test childObject32 = new Test("childtest32", "childTitle", "childAuthor", 185);
        Test childObject33 = new Test("childtest33", "childTitle", "childAuthor", 315);

        Test subchildObject31 = new Test("childtest311", "childTitle", "subChildAuthor", 33300);
        Test subchildObject32 = new Test("childtest321", "childTitle", "childAuthor", 2185);
        Test subchildObject33 = new Test("childtest331", "childTitle", "childAuthor", 2315);

        childObject1.setChildren(Arrays.asList(subchildObject31, subchildObject32, subchildObject33));

        childlist.addAll(Arrays.asList(childObject1, childObject2, childObject3));
        childlist2.addAll(Arrays.asList(childObject21, childObject22, childObject23));
        childlist3.addAll(Arrays.asList(childObject31, childObject32, childObject33));
        testObject.setChildren(childlist);
        testObject2.setChildren(childlist2);
        testObject3.setChildren(childlist3);
        List<Test> list = new ArrayList<>(Arrays.asList(testObject, testObject2, testObject3));
        return list;
    }
}
