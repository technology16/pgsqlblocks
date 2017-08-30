package ru.taximaxim.pgsqlblocks.common.ui;

import ru.taximaxim.pgsqlblocks.common.FilterCondition;

public interface DBProcessesFiltersViewListener {

    void processesFiltersViewPidFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition);

    void processesFiltersViewPidFilterValueChanged(DBProcessesFiltersView view, Integer value);

    void processesFiltersViewQueryFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition);

    void processesFiltersViewQueryFilterValueChanged(DBProcessesFiltersView view, String value);

    void processesFiltersViewApplicationFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition);

    void processesFiltersViewApplicationFilterValueChanged(DBProcessesFiltersView view, String value);

    void processesFiltersViewDatabaseFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition);

    void processesFiltersViewDatabaseFilterValueChanged(DBProcessesFiltersView view, String value);

    void processesFiltersViewUserNameFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition);

    void processesFiltersViewUserNameFilterValueChanged(DBProcessesFiltersView view, String value);

    void processesFiltersViewClientFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition);

    void processesFiltersViewClientFilterValueChanged(DBProcessesFiltersView view, String value);

}
