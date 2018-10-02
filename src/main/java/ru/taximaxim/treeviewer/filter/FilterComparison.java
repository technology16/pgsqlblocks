package ru.taximaxim.treeviewer.filter;

import ru.taximaxim.treeviewer.utils.ColumnType;

import java.util.Date;

/**
 * Класс, который производит сравнение данных объекта с данными фильтра
 */
public class FilterComparison {

    private ColumnType columnType;

    public boolean comparison(String objectValue, String searchValue){
        return comparison(objectValue, searchValue, FilterValues.CONTAINS, ColumnType.STRING);
    }

    public boolean comparison(String objectValue, String searchValue, FilterValues filter, ColumnType columnType){
        this.columnType = columnType;
        switch (filter) {
            case NONE:
                return true;
            case EQUALS:
                return objectValue.equals(searchValue);
            case NOT_EQUALS:
                return !objectValue.equals(searchValue);
            case CONTAINS:
                return objectValue.toLowerCase().contains(searchValue.toLowerCase());
            case GREATER:
                return greater(objectValue, searchValue);
            case GREATER_OR_EQUAL:
                return greaterOrEquals(objectValue, searchValue);
            case LESS:
                return less(objectValue, searchValue);
            case LESS_OR_EQUAL:
                return lessOrEquals(objectValue, searchValue);
        }
        return true;
    }

    private boolean greater(String objectValue, String searchValue) {
        switch (columnType) {
            case INTEGER:
                return getInteger(objectValue) > getInteger(searchValue);
            case DOUBLE:
                return getDouble(objectValue) > getDouble(searchValue);
            case DATE:
                //return getDate(objectValue).after(getDate(searchValue));
            case STRING:
                return objectValue.compareTo(searchValue) > 0;
        }
        return false;
    }

    private boolean greaterOrEquals(String objectValue, String searchValue) {
        switch (columnType) {
            case INTEGER:
                return getInteger(objectValue) >= getInteger(searchValue);
            case DOUBLE:
                return getDouble(objectValue) >= getDouble(searchValue);
            case DATE:
//                Date objectDate = getDate(objectValue);
//                Date searchDate = getDate(searchValue);
//                return objectDate.after(searchDate) || objectDate.equals(searchDate);
            case STRING:
                return objectValue.compareTo(searchValue) >= 0;
        }
        return false;
    }

    private boolean less(String objectValue, String searchValue) {
        switch (columnType) {
            case INTEGER:
                return getInteger(objectValue) < getInteger(searchValue);
            case DOUBLE:
                return getDouble(objectValue) < getDouble(searchValue);
            case DATE:
                //return getDate(objectValue).before(getDate(searchValue));
            case STRING:
                return objectValue.compareTo(searchValue) < 0;
        }
        return false;
    }

    private boolean lessOrEquals(String objectValue, String searchValue) {
        switch (columnType) {
            case INTEGER:
                return getInteger(objectValue) <= getInteger(searchValue);
            case DOUBLE:
                return getDouble(objectValue) <= getDouble(searchValue);
            case DATE:
//                Date objectDate = getDate(objectValue);
//                Date searchDate = getDate(searchValue);
//                return objectDate.before(searchDate) || objectDate.equals(searchDate);
            case STRING:
                return objectValue.compareTo(searchValue) <= 0;
        }
        return false;
    }

    private int getInteger(String value){
        int i;
        try{
            i = Integer.parseInt(value);
        }catch (NumberFormatException e){
            i = 1;
        }
        return i;
    }

    private double getDouble(String value){
        double d;
        try {
            d = Double.parseDouble(value);
        }catch (NumberFormatException e){
            d = 1.0;
        }
        return d;
    }

    // TODO: 01.10.18 parse date!!!!
    private Date getDate(String value){
        return new Date();
    }
}
