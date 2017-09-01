package ru.taximaxim.pgsqlblocks.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum FilterCondition {

    NONE(""),
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    LESS("<"),
    LESS_OR_EQUAL("<="),
    CONTAINS("~");

    private final String conditionText;

    FilterCondition(String conditionText) {
        this.conditionText = conditionText;
    }

    @Override
    public String toString() {
        return conditionText;
    }

    public static List<FilterCondition> getConditionsForValueType(FilterValueType valueType) {
        List<FilterCondition> result = new ArrayList<>();
        result.add(NONE);
        switch (valueType) {
            case INTEGER:
                result.add(EQUALS);
                result.add(NOT_EQUALS);
                result.add(GREATER);
                result.add(GREATER_OR_EQUAL);
                result.add(LESS);
                result.add(LESS_OR_EQUAL);
                break;
            case STRING:
                result.add(EQUALS);
                result.add(CONTAINS);
                break;
        }
        return result;
    }

    public static FilterCondition getFilterConditionFromConditionText(String conditionText) {
        Optional<FilterCondition> filterCondition = Arrays.stream(FilterCondition.values())
                .filter(fc -> fc.conditionText.equals(conditionText))
                .findFirst();
        return filterCondition.orElse(FilterCondition.NONE);
    }
}
