package ru.taximaxim.pgsqlblocks.common;

public class StringValueTypeFilter extends Filter<String> {

    public StringValueTypeFilter() {
        super(FilterValueType.STRING);
    }

    @Override
    public boolean filter(String actualValue) {
        if (!isActive()) {
            return true;
        }
        switch (condition) {
            case NONE:
                return true;
            case EQUALS:
                return actualValue.equals(value);
            case CONTAINS:
                return actualValue.toLowerCase().contains(value.toLowerCase());
            default:
                return true;
        }
    }

    @Override
    public boolean isActive() {
        return super.isActive() && !value.isEmpty();
    }
}
