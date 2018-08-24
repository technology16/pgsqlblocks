package ru.taximaxim.treeviewer.filter;

import ru.taximaxim.treeviewer.listeners.DataUpdateListener;
import ru.taximaxim.treeviewer.listeners.FilterListener;
import ru.taximaxim.treeviewer.listeners.ViewFilterListener;
import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Класс вызывающий фильтр и затем обновление данных
 */
public class ViewFilter implements ViewFilterListener {

    private IColumn column;
    private String searchText = "";
    private FilterValues value = FilterValues.CONTAINS;
    private FilterListener filterListener;
    private DataUpdateListener dataUpdateListener;

    public ViewFilter(IColumn column, FilterListener filterListener, DataUpdateListener dataUpdateListener) {
        this.column = column;
        this.filterListener = filterListener;
        this.dataUpdateListener = dataUpdateListener;
    }

    @Override
    public void onTextChanges(String text) {
        this.searchText = text;
        filterListener.filter(ViewFilter.this);
        dataUpdateListener.needUpdateData();
        System.out.println(column.getColumnName() + " " + searchText + " " + value.getConditionText());
    }

    @Override
    public void onComboChanges(FilterValues value) {
        this.value = value;
        filterListener.filter(this);
        dataUpdateListener.needUpdateData();
        System.out.println(column.getColumnName() + " " + searchText + " " + value.getConditionText());
    }
}
