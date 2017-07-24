package ru.taximaxim.pgsqlblocks.common;

public interface FilterListener {

    void filterDidActivate(Filter filter);

    void filterDidDeactivate(Filter filter);

    void filterValueChanged(Filter filter);

    void filterConditionChanged(Filter filter);

}
