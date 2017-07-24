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
        }
        return true;
    }

    @Override
    public boolean isActive() {
        boolean isActive = super.isActive();
        if (!isActive)
            return isActive;
        return isActive && !value.isEmpty();
    }
}
