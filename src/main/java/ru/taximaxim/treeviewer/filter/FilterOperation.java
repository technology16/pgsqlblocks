package ru.taximaxim.treeviewer.filter;

import ru.taximaxim.treeviewer.utils.ColumnType;
import ru.taximaxim.treeviewer.utils.TriFunction;

import java.util.Date;
import java.util.Optional;

/**
 * Enum for types of column filters
 */
public enum FilterOperation {

    NONE("", (o, s, t) -> true),
    EQUALS("=", (o, s, t) -> equalsByType(o, s, t)),
    NOT_EQUALS("!=", (o, s, t) -> !EQUALS.matchesForType(o, s, t)),
    CONTAINS("~", (o, s, t) -> o.toLowerCase().contains(s.toLowerCase())),
    GREATER(">", (o, s, t) -> greater(o, s, t)),
    GREATER_OR_EQUAL(">=", (o, s, t) -> EQUALS.matchesForType(o, s, t) || GREATER.matchesForType(o, s, t)),
    LESS("<", (o, s, t) -> less(o, s, t)),
    LESS_OR_EQUAL("<=", (o, s, t) -> EQUALS.matchesForType(o, s, t) || LESS.matchesForType(o, s, t));

    private final String conditionText;
    private final TriFunction<String, String, ColumnType, Boolean> testFunction;

    FilterOperation(String conditionText, TriFunction<String, String, ColumnType, Boolean> testFunction) {
        this.conditionText = conditionText;
        this.testFunction = testFunction;
    }

    public boolean matchesForType(String objectValue, String searchValue, ColumnType columnType) {
        return testFunction.apply(objectValue, searchValue, columnType);
    }

    @Override
    public String toString() {
        return conditionText;
    }

    public String getConditionText() {
        return conditionText;
    }

    public static FilterOperation find(String text) {
        FilterOperation[] list = FilterOperation.values();
        for (FilterOperation filterOperation : list) {
            if (filterOperation.getConditionText().equals(text)) {
                return filterOperation;
            }
        }
        return FilterOperation.NONE;
    }

    private static boolean equalsByType(String objectValue, String searchValue, ColumnType columnType) {
        switch (columnType) {
            case INTEGER:
                Optional objectInt = getInteger(objectValue);
                Optional searchInt = getInteger(searchValue);
                if (!objectInt.isPresent() || !searchInt.isPresent()) {
                    return false;
                } else {
                    return objectInt == searchInt;
                }
            case DOUBLE:
                Optional<Double> objectDouble = getDouble(objectValue);
                Optional<Double> searchDouble = getDouble(searchValue);
                if (!objectDouble.isPresent() || !searchDouble.isPresent()) {
                    return false;
                } else {
                    return objectDouble == searchDouble;
                }
            case DATE:
//                Date objectDate = getDate(objectValue);
//                Date searchDate = getDate(searchValue);
//                return objectDate.equals(searchDate);
            case STRING:
                return objectValue.equals(searchValue);
        }
        return false;
    }

    private static boolean greater(String objectValue, String searchValue, ColumnType columnType) {
        switch (columnType) {
            case INTEGER:
                Optional<Integer> objectInt = getInteger(objectValue);
                Optional<Integer> searchInt = getInteger(searchValue);
                if (!objectInt.isPresent() || !searchInt.isPresent()) {
                    return false;
                } else {
                    return objectInt.get() > searchInt.get();
                }
            case DOUBLE:
                Optional<Double> objectDouble = getDouble(objectValue);
                Optional<Double> searchDouble = getDouble(searchValue);
                if (!objectDouble.isPresent() || !searchDouble.isPresent()) {
                    return false;
                } else {
                    return objectDouble.get() > searchDouble.get();
                }
            case DATE:
                //return getDate(objectValue).after(getDate(searchValue));
            case STRING:
                return objectValue.compareTo(searchValue) > 0;
        }
        return false;
    }

    private static boolean less(String objectValue, String searchValue, ColumnType columnType) {
        switch (columnType) {
            case INTEGER:
                Optional<Integer> objectInt = getInteger(objectValue);
                Optional<Integer> searchInt = getInteger(searchValue);
                if (!objectInt.isPresent() || !searchInt.isPresent()) {
                    return false;
                } else {
                    return objectInt.get() < searchInt.get();
                }
            case DOUBLE:
                Optional<Double> objectDouble = getDouble(objectValue);
                Optional<Double> searchDouble = getDouble(searchValue);
                if (!objectDouble.isPresent() || !searchDouble.isPresent()) {
                    return false;
                } else {
                    return objectDouble.get() < searchDouble.get();
                }
            case DATE:
                //return getDate(objectValue).before(getDate(searchValue));
            case STRING:
                return objectValue.compareTo(searchValue) < 0;
        }
        return false;
    }

    private static Optional<Integer> getInteger(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Double> getDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    // TODO: 01.10.18 parse date!!!!
    private Optional<Date> getDate(String value) {
        return Optional.empty();
    }
}
