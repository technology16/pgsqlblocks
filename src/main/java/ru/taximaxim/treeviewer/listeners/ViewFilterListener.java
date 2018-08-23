package ru.taximaxim.treeviewer.listeners;

import ru.taximaxim.treeviewer.filter.FilterValues;

/**
 * Created by user on 23.08.18.
 */
public interface ViewFilterListener {

    void onTextChanges(String text);
    void onComboChanges(FilterValues value);
}
