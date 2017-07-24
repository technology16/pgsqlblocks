package ru.taximaxim.pgsqlblocks.common.ui;

import ru.taximaxim.pgsqlblocks.common.FilterCondition;

public interface DBProcessesFiltersViewListener {

    void pidFilterConditionChanged(FilterCondition condition);

    void pidFilterValueChanged(Integer value);

}
