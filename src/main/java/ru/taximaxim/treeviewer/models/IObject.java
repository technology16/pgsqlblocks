package ru.taximaxim.treeviewer.models;

import java.util.List;

/**
 * Methods which need to implement in model class
 */
public interface IObject {

    List<? extends IObject> getChildren();

    boolean hasChildren();
}
