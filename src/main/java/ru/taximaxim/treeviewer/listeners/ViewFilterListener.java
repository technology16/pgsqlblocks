package ru.taximaxim.treeviewer.listeners;

import ru.taximaxim.treeviewer.filter.FilterValues;

/**
 * Listener for actions in gui of filter
 */
public interface ViewFilterListener {

    void onTextChanges(String text);
    void onComboChanges(FilterValues value);
    void onAllTextChanges(String text);
}
