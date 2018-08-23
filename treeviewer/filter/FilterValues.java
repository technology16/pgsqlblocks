package ru.taximaxim.treeviewer.filter;


public enum  FilterValues {

    NONE(""),
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    LESS("<"),
    LESS_OR_EQUAL("<="),
    CONTAINS("~");

    private final String conditionText;

    FilterValues(String conditionText) {
        this.conditionText = conditionText;
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
