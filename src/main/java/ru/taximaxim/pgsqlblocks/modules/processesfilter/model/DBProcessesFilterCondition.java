package ru.taximaxim.pgsqlblocks.modules.processesfilter.model;

import java.util.List;

public enum DBProcessesFilterCondition {

    NONE,
    EQUALS,
    NOT_EQUALS,
    GREATER,
    GREATER_OR_EQUAL,
    LESS,
    LESS_OR_EQUAL,
    CONTAINS;

    @Override
    public String toString() {
        switch (this) {
            case NONE:
                return "";
            case EQUALS:
                return "=";
            case NOT_EQUALS:
                return "!=";
            case GREATER:
                return ">";
            case GREATER_OR_EQUAL:
                return ">=";
            case LESS:
                return "<";
            case LESS_OR_EQUAL:
                return "<=";
            case CONTAINS:
                return "~";
            default:
                return "";
        }
    }
}
