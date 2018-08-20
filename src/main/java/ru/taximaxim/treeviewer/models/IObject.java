package ru.taximaxim.treeviewer.models;

import java.util.List;

/**
 * Общие методы, которые обязательно должны быть в объекте
 */
public interface IObject<T> {
    List<T> getChildren();

    boolean hasChildren();
}
