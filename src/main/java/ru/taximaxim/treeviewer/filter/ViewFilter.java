package ru.taximaxim.treeviewer.filter;

import ru.taximaxim.treeviewer.listeners.AllTextFilterListener;
import ru.taximaxim.treeviewer.listeners.DataUpdateListener;
import ru.taximaxim.treeviewer.listeners.FilterListener;
import ru.taximaxim.treeviewer.listeners.ViewFilterListener;
import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Class for filter's listeners. It controls filtering and update data
 */
public class ViewFilter implements ViewFilterListener {

    private IColumn column;
    private String searchText = "";
    private FilterValues value = FilterValues.CONTAINS;
    private FilterListener filterListener;
    private AllTextFilterListener allTextFilterListener;
    private DataUpdateListener dataUpdateListener;

    public ViewFilter(IColumn column, FilterListener filterListener, AllTextFilterListener allTextFilterListener,
                      DataUpdateListener dataUpdateListener) {
        this.column = column;
        this.filterListener = filterListener;
        this.dataUpdateListener = dataUpdateListener;
        this.allTextFilterListener = allTextFilterListener;
    }

    @Override
    public void onTextChanges(String text) {
        this.searchText = text;
        filterListener.filter(ViewFilter.this);
        dataUpdateListener.needUpdateData();
    }

    @Override
    public void onComboChanges(FilterValues value) {
        this.value = value;
        filterListener.filter(this);
        dataUpdateListener.needUpdateData();
    }

    @Override
    public void onAllTextChanges(String text) {
        this.searchText = text;
        allTextFilterListener.filterAllColumn(this);
    }

    public IColumn getColumn() {
        return column;
    }

    public String getSearchText() {
        return searchText;
    }

    public FilterValues getValue() {
        return value;
    }
}
