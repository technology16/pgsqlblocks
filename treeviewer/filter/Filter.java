package ru.taximaxim.treeviewer.filter;

import ru.taximaxim.treeviewer.listeners.FilterListener;
import ru.taximaxim.treeviewer.models.IObject;

import java.util.List;

/**
 * Created by user on 24.08.18.
 */
public abstract class Filter implements FilterListener {


    public abstract List<? extends IObject> getFilteredList();

    public abstract boolean matchingObject(Object object);


}
