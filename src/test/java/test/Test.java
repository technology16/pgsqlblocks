package test;

import ru.taximaxim.treeviewer.models.IObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 17.08.18.
 */
public class Test implements IObject {
    private String name;
    private String title;
    private String author;
    private int price;

    private List<Test> children = new ArrayList<>();

    public Test(String name, String title, String author, int price) {
        this.name = name;
        this.title = title;
        this.author = author;
        this.price = price;
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

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPrice() {
        return price;
    }

    public void setChildren(List<Test> children) {
        this.children = children;
    }
}
