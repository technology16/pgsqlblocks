package ru.taximaxim.treeviewer.listeners;

import ru.taximaxim.treeviewer.filter.ViewFilter;

/**
 * слушатель по всему тексту всех доступных колонок
 */
public interface AllTextFilterListener {

    void filterAllColumn(ViewFilter filter);
}
