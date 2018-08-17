package test;

import ru.taximaxim.treeviewer.utils.IObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 17.08.18.
 */
public class Test implements IObject {
    private String name;
    private List<Test> children = new ArrayList<>();

    public Test(String name) {
        this.name = name;
    }

    @Override
    public List getChildren() {
        return children;
    }


    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setChildren(List<Test> children) {
        this.children = children;
    }
}
