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
}
