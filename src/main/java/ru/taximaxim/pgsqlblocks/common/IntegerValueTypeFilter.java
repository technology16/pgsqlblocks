package ru.taximaxim.pgsqlblocks.common;

public class IntegerValueTypeFilter extends Filter<Integer> {

    public IntegerValueTypeFilter() {
        super(FilterValueType.INTEGER);
    }

    @Override
    public boolean filter(Integer actualValue) {
        if (!isActive()) {
            return true;
        }
        switch (condition) {
            case NONE:
                return true;
            case EQUALS:
                return actualValue.equals(value);
            case NOT_EQUALS:
                return !actualValue.equals(value);
            case GREATER:
                return actualValue > value;
            case GREATER_OR_EQUAL:
                return actualValue >= value;
            case LESS:
                return actualValue < value;
            case LESS_OR_EQUAL:
                return actualValue <= value;
            default:
                return true;
        }
    }
}
