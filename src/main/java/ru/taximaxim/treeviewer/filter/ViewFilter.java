package ru.taximaxim.treeviewer.filter;

import ru.taximaxim.treeviewer.listeners.ViewFilterListener;
import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Created by user on 23.08.18.
 */
public class ViewFilter implements ViewFilterListener {
    private IColumn column;
    private String searchText;
    private FilterValues value;

    public ViewFilter(IColumn column) {
        this.column = column;
    }

    @Override
    public void onTextChanges(String text) {
        this.searchText = text;
        //тут какой-то внешний листенер

        System.out.println(column.getColumnName() + " " + searchText + " " + value.getConditionText());
    }

    @Override
    public void onComboChanges(FilterValues value) {
        this.value = value;
        System.out.println(column.getColumnName() + " " + searchText + " " + value.getConditionText());
    }
}
