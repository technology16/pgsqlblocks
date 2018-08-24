package newview;

import ru.taximaxim.treeviewer.listeners.Filterable;
import ru.taximaxim.treeviewer.filter.ViewFilter;
import ru.taximaxim.treeviewer.models.IObject;
import test.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 24.08.18.
 */
public class MyFilter implements Filterable {
    private List<Test> tests = new ArrayList<>();
    private List<Test> filtered = new ArrayList<>();
    private ViewFilter filter;

    public MyFilter(List<Test> tests) {
        this.tests = tests;
    }

    @Override
    public void filter(ViewFilter filter) {
        this.filter = filter;
        getFilteredList();
        System.out.println("TUUUUUUUUUUUU");
    }

    @Override
    public List<? extends IObject> getFilteredList() {
        System.out.println("in my filter!!!!");
        return tests;
    }

    @Override
    public boolean matchingObject(Object object) {
        return false;
    }
}
