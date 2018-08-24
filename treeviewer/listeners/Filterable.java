package ru.taximaxim.treeviewer.listeners;

import ru.taximaxim.treeviewer.models.IObject;

import java.util.List;

/**
 * Interface for creating filterable columns
 */
public interface Filterable extends FilterListener {

    List<? extends IObject> getFilteredList();

    boolean matchingObject(Object object);


}
