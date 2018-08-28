package ru.taximaxim.treeviewer.models;

import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.TreeColumn;
import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Класс необходим для сортировки колонки по возрастанию/убыванию
 * setColumn должен получает колонку treeColumn.getData()
 * setSortDirection получает тип сортировки.
 */
public abstract class ObjectViewComparator extends ViewerComparator {

    public ObjectViewComparator() {
        super();
    }

    public abstract void setColumn(TreeColumn column);

    public abstract void setSortDirection(int sortDirection);

    public abstract int compareIntegerValues(int one, int two);

    public abstract int compareStringValues(String one, String two);

    public abstract int compareBooleans(boolean one, boolean two);
}
