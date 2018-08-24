package ru.taximaxim.treeviewer.listeners;

import ru.taximaxim.treeviewer.filter.FilterValues;

/**
 * Слушатель для событий происходящий во вью фильтра
 */
public interface ViewFilterListener {

    void onTextChanges(String text);
    void onComboChanges(FilterValues value);
}
