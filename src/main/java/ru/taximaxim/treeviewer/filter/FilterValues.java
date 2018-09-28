package ru.taximaxim.treeviewer.filter;

import java.util.function.BiFunction;

/**
 * Enum for types of column filters
 */
public enum FilterValues {

    NONE("", (objectValue, searchValue) -> true),
    EQUALS("=", (objectValue, searchValue) -> objectValue.equals(searchValue)),
    NOT_EQUALS("!=", (objectValue, searchValue) -> !objectValue.equals(searchValue)),
    CONTAINS("~", (objectValue, searchValue) -> objectValue.toLowerCase().contains(searchValue.toLowerCase())),
    GREATER(">", (objectValue, searchValue) -> getInteger(objectValue) > getInteger(searchValue)), // FIXME а что с double? date?
    GREATER_OR_EQUAL(">=", (objectValue, searchValue) -> getInteger(objectValue) >= getInteger(searchValue)),
    LESS("<", (objectValue, searchValue) -> getInteger(objectValue) < getInteger(searchValue)),
    LESS_OR_EQUAL("<=", (objectValue, searchValue) -> getInteger(objectValue) <= getInteger(searchValue));


    private final String conditionText;
    private final BiFunction<String, String, Boolean> compareFunction;

    FilterValues(String conditionText, BiFunction<String, String, Boolean> compareFunction) {
        this.conditionText = conditionText;
        this.compareFunction = compareFunction;
    }

    public boolean comparison(String objectValue, String searchValue) {
        return compareFunction.apply(objectValue, searchValue);
    }

    private static Integer getInteger(String value){
        // FIXME 1? Все строки будут равны 1? Форматирование поправить (пробелы), заменить Integer на примитив
        Integer i;
        try {
            i = Integer.parseInt(value);
        }catch (NumberFormatException e){
            i = 1;
        }
        return i;
    }

    @Override
    public String toString() {
        return conditionText;
    }

    public String getConditionText() {
        return conditionText;
    }

    public static FilterValues find(String text) {
        FilterValues[] list = FilterValues.values();
        for (FilterValues filterValues : list) {
            if (filterValues.getConditionText().equals(text)) {
                return filterValues;
            }
        }
        return FilterValues.NONE;
    }
}