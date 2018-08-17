package test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 16.08.18.
 */
public class TestObject {

    private final String name;
    private List<TestObject> children = new ArrayList<>();

    public TestObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setChildren(List<TestObject> children) {
        this.children = children;
    }

    public List<TestObject> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
