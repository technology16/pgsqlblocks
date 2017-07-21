package ru.taximaxim.pgsqlblocks.modules.processesfilter.model;

import ru.taximaxim.pgsqlblocks.common.models.DBProcess;

public class DBProcessesPidFilter extends DBProcessesFilter<Integer> {

    public DBProcessesPidFilter() {
        supportedConditions = new DBProcessesFilterCondition[] {
                DBProcessesFilterCondition.NONE,
                DBProcessesFilterCondition.EQUALS,
                DBProcessesFilterCondition.NOT_EQUALS,
                DBProcessesFilterCondition.GREATER,
                DBProcessesFilterCondition.GREATER_OR_EQUAL,
                DBProcessesFilterCondition.LESS,
                DBProcessesFilterCondition.LESS_OR_EQUAL
        };
    }

    @Override
    public boolean filter(DBProcess process) {
        if (!isActive())
            return true;
        switch (selectedCondition) {
            case NONE:
                return true;
            case EQUALS:
                return process.getPid() == value;
            case NOT_EQUALS:
                return process.getPid() != value;
            case GREATER:
                return process.getPid() > value;
            case GREATER_OR_EQUAL:
                return process.getPid() >= value;
            case LESS:
                return process.getPid() < value;
            case LESS_OR_EQUAL:
                return process.getPid() <= value;
            default:
                return true;
        }
    }
}
