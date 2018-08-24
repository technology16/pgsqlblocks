package ru.taximaxim.treeviewer.listeners;


import ru.taximaxim.treeviewer.filter.ViewFilter;

/**
 * Слушатель для фильтра. Необходимо при реализации в конструктор передавать список объектов и датасурс либо лист с колонками
 */
public interface FilterListener {

    void filter(ViewFilter filter);
}
