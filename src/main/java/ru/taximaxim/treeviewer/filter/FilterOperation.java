package ru.taximaxim.treeviewer.filter;

import ru.taximaxim.treeviewer.utils.ColumnType;
import ru.taximaxim.treeviewer.utils.TriFunction;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Enum for types of column filters
 */
enum FilterOperation {

    NONE("", (o, s, t) -> true),
    EQUALS("=", (o, s, t) -> equalsByType(o, s, t)),
    NOT_EQUALS("!=", (o, s, t) -> !EQUALS.matchesForType(o, s, t)),
    CONTAINS("~", (o, s, t) -> o.toLowerCase().contains(s.toLowerCase())),
    GREATER(">", (o, s, t) -> greater(o, s, t)),
    GREATER_OR_EQUAL(">=", (o, s, t) -> EQUALS.matchesForType(o, s, t) || GREATER.matchesForType(o, s, t)),
    LESS("<", (o, s, t) -> less(o, s, t)),
    LESS_OR_EQUAL("<=", (o, s, t) -> EQUALS.matchesForType(o, s, t) || LESS.matchesForType(o, s, t));

    private final String conditionText;
    private final TriFunction<String, String, ColumnType, Boolean> matcherFunction;

    FilterOperation(String conditionText, TriFunction<String, String, ColumnType, Boolean> matcherFunction) {
        this.conditionText = conditionText;
        this.matcherFunction = matcherFunction;
    }

    boolean matchesForType(String objectValue, String searchValue, ColumnType columnType) {
        return matcherFunction.apply(objectValue, searchValue, columnType);
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
                return matches(objectValue, searchValue, FilterOperation::getInteger, Integer::equals);
            case DOUBLE:
                return matches(objectValue, searchValue, FilterOperation::getDouble, Double::equals);
            case DATE:
            case STRING:
                return objectValue.equals(searchValue);
        }
        return false;
    }

    private static boolean greater(String objectValue, String searchValue, ColumnType columnType) {
        switch (columnType) {
            case INTEGER:
                return matches(objectValue, searchValue, FilterOperation::getInteger, (i1, i2) -> i1 > i2);
            case DOUBLE:
                return matches(objectValue, searchValue, FilterOperation::getDouble, (i1, i2) -> i1 > i2);
            case DATE:
            case STRING:
                return objectValue.compareTo(searchValue) > 0;
        }
        return false;
    }

    private static boolean less(String objectValue, String searchValue, ColumnType columnType) {
        switch (columnType) {
            case INTEGER:
                return matches(objectValue, searchValue, FilterOperation::getInteger, (i1, i2) -> i1 < i2);
            case DOUBLE:
                return matches(objectValue, searchValue, FilterOperation::getDouble, (i1, i2) -> i1 < i2);
            case DATE:
            case STRING:
                return objectValue.compareTo(searchValue) < 0;
        }
        return false;
    }

    private static <T> boolean matches(String objectValue, String searchValue,
                                       Function<String, Optional<T>> converter, BiFunction<T, T, Boolean> matcher) {
        Optional<T> objectOpt = converter.apply(objectValue);
        Optional<T> searchOpt = converter.apply(searchValue);
        return objectOpt.isPresent() && searchOpt.isPresent() && matcher.apply(objectOpt.get(), searchOpt.get());
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
}
