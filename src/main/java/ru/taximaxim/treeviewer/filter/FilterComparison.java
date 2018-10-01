package ru.taximaxim.treeviewer.filter;

import ru.taximaxim.treeviewer.utils.ColumnType;

import java.util.function.BiFunction;

/**
 * Класс, который производит сравнение данных объекта с данными фильтра
 */
public class FilterComparison {

    private BiFunction<String, String, Boolean> compareFunction;

    public boolean comparison(String objectValue, String searchValue){
        return comparison(objectValue, searchValue, FilterValues.CONTAINS, ColumnType.STRING);
    }

    public boolean comparison(String objectValue, String searchValue,
                              FilterValues filter, ColumnType columnType){
        return true;
    }

}
