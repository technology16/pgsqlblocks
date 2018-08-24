package newview;

import ru.taximaxim.treeviewer.listeners.FilterListener;
import ru.taximaxim.treeviewer.filter.ViewFilter;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;
import test.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 24.08.18.
 */
public class MyFilter implements FilterListener {
    private List<Test> tests = new ArrayList<>();
    private List<Test> filtered = new ArrayList<>();
    private List<IColumn> columnlist = new ArrayList<>();
    private ViewFilter filter;
    private MyTreeViewerDataSource dataSource;

    public MyFilter(List<Test> tests, MyTreeViewerDataSource dataSource) {
        this.tests = tests;
        this.filtered = tests;
        this.dataSource = dataSource;
        columnlist.addAll(dataSource.getColumns());
    }

    @Override
    public void filter(ViewFilter filter) {
        this.filter = filter;
        //тут делаем все что угодно, но потом сохраняем в
        //tests.add(new Test("title"));
        tests.clear();
        //tests.addAll(getFilteredList());
        System.out.println("TUUUUUUUUUUUU");
    }

}
