package ru.taximaxim.pgsqlblocks.common.ui;

import ru.taximaxim.pgsqlblocks.common.FilterCondition;

public interface DBProcessesFiltersViewListener {

    void processesFiltersViewPidFilterConditionChanged(FilterCondition condition);

    void processesFiltersViewPidFilterValueChanged(Integer value);

    void processesFiltersViewQueryFilterConditionChanged(FilterCondition condition);

    void processesFiltersViewQueryFilterValueChanged(String value);

    void processesFiltersViewApplicationFilterConditionChanged(FilterCondition condition);

    void processesFiltersViewApplicationFilterValueChanged(String value);

}
