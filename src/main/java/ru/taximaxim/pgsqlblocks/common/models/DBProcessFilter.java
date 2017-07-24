package ru.taximaxim.pgsqlblocks.common.models;

import ru.taximaxim.pgsqlblocks.common.Filter;
import ru.taximaxim.pgsqlblocks.common.FilterListener;
import ru.taximaxim.pgsqlblocks.common.IntegerValueTypeFilter;

import java.util.ArrayList;
import java.util.List;

public class DBProcessFilter implements FilterListener {

    private final List<DBProcessFilterListener> listeners = new ArrayList<>();

    private final IntegerValueTypeFilter pidFilter = new IntegerValueTypeFilter();

    public DBProcessFilter() {
        pidFilter.addListener(this);
    }

    public boolean filter(DBProcess process) {
        boolean pidFilterResult = !pidFilter.isActive() || pidFilter.filter(process.getPid());
        return pidFilterResult;
    }

    public void resetFilters() {
        pidFilter.reset();
        listeners.forEach(DBProcessFilterListener::dbProcessFilterChanged);
    }

    public IntegerValueTypeFilter getPidFilter() {
        return pidFilter;
    }

    public void addListener(DBProcessFilterListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBProcessFilterListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void filterDidActivate(Filter filter) {
        listeners.forEach(DBProcessFilterListener::dbProcessFilterChanged);
    }

    @Override
    public void filterDidDeactivate(Filter filter) {
        listeners.forEach(DBProcessFilterListener::dbProcessFilterChanged);
    }

    @Override
    public void filterValueChanged(Filter filter) {
        if (filter.isActive()) {
            listeners.forEach(DBProcessFilterListener::dbProcessFilterChanged);
        }
    }

    @Override
    public void filterConditionChanged(Filter filter) {
        if (filter.isActive()) {
            listeners.forEach(DBProcessFilterListener::dbProcessFilterChanged);
        }
    }
}
