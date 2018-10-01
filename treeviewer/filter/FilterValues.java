package ru.taximaxim.treeviewer.filter;

import java.util.Date;
import java.util.function.BiFunction;

/**
 * Enum for types of column filters
 */
public enum FilterValues {

//    NONE("", (objectValue, searchValue) -> true),
//    EQUALS("=", (objectValue, searchValue) -> objectValue.equals(searchValue)),
//    NOT_EQUALS("!=", (objectValue, searchValue) -> !objectValue.equals(searchValue)),
//    CONTAINS("~", (objectValue, searchValue) -> objectValue.toLowerCase().contains(searchValue.toLowerCase())),
//    GREATER(">", (objectValue, searchValue) -> greaterFunction(objectValue, searchValue)), // FIXME а что с double? date?
//    GREATER_OR_EQUAL(">=", (objectValue, searchValue) -> getInteger(objectValue) >= getInteger(searchValue)), // FIXME а что с double? date?
//    LESS("<", (objectValue, searchValue) -> getInteger(objectValue) < getInteger(searchValue)), // FIXME а что с double? date?
//    LESS_OR_EQUAL("<=", (objectValue, searchValue) -> getInteger(objectValue) <= getInteger(searchValue));
    NONE(""),
    EQUALS("="),
    NOT_EQUALS("!="),
    CONTAINS("~"),
    GREATER(">"), // FIXME а что с double? date?
    GREATER_OR_EQUAL(">="), // FIXME а что с double? date?
    LESS("<"), // FIXME а что с double? date?
    LESS_OR_EQUAL("<=");
    //сравнения изменить на отдельный метод а не прямой вызов. А внутри сделать метод парсер, который возвращает тип.
    // после возвращения типа производить парсер определенного типа и затем проводить сравнение

    //нееее. сделать в лямбде тело и в нем определять тип, а потом тупо перегрузить методы!!!!!
    private final String conditionText;
   // private final BiFunction<String, String, Boolean> compareFunction;

    FilterValues(String conditionText) {
        this.conditionText = conditionText;
        //this.compareFunction = compareFunction;
    }

//    public boolean comparison(String objectValue, String searchValue) {
//        return compareFunction.apply(objectValue, searchValue);
//    }

    private static Boolean greaterFunction(String objectValue, String searchValue) {
        int type = getType(objectValue);
        switch (type) {
            case 1: //integer
                return getInteger(objectValue) > getInteger(searchValue);
            case 2: //double
                return getDouble(objectValue) > getDouble(searchValue);
            case 3: //date
                return getDate(objectValue).after(getDate(searchValue));
        }
        return false;
    }

    private static int getType(String text){
        if (text.matches("[0-9]+")){
            return 1;
        }else if (text.matches("[0-9]+\\.[0-9]+")){
            return 2;
        }
        return 3;
    }

    private static int getInteger(String value){
        int i;
        try{
            i = Integer.parseInt(value);
        }catch (NumberFormatException e){
            i = 1;
        }
        return i;
    }

    private static double getDouble(String value){
        double d;
        try {
            d = Double.parseDouble(value);
        }catch (NumberFormatException e){
            d = 1.0;
        }
        return d;
    }

    private static Date getDate(String value){

        return new Date();
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