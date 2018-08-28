package newview;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Test test = (Test) o;

        if (price != test.price) return false;
        if (name != null ? !name.equals(test.name) : test.name != null) return false;
        if (title != null ? !title.equals(test.title) : test.title != null) return false;
        return author != null ? author.equals(test.author) : test.author == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + price;
        return result;
    }

    /**
     * добавить поиск по наследникам
     */
    public boolean isForAllFilter(String searchText) {
        if (name.contains(searchText)) {
            return true;
        }
        if (title.contains(searchText)) {
            return true;
        }
        if (author.contains(searchText)) {
            return true;
        }
        if (String.valueOf(price).contains(searchText)) {
            return true;
        }
        return false;
    }
}
