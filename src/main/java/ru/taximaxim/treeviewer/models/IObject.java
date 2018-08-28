package ru.taximaxim.treeviewer.models;

import java.util.List;

/**
 * Methods which need to implement in model class
 */
public interface IObject<T> {
    List<T> getChildren();

    boolean hasChildren();

    boolean isForAllTextFilter(String searchText);
}
