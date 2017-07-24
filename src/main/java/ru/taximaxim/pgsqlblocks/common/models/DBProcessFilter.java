package ru.taximaxim.pgsqlblocks.common.models;

import ru.taximaxim.pgsqlblocks.common.Filter;
import ru.taximaxim.pgsqlblocks.common.FilterListener;
import ru.taximaxim.pgsqlblocks.common.IntegerValueTypeFilter;
import ru.taximaxim.pgsqlblocks.common.StringValueTypeFilter;

import java.util.ArrayList;
import java.util.List;

public class DBProcessFilter implements FilterListener {

    private final List<DBProcessFilterListener> listeners = new ArrayList<>();

    private final IntegerValueTypeFilter pidFilter = new IntegerValueTypeFilter();

    private final StringValueTypeFilter applicationFilter = new StringValueTypeFilter();

    private final StringValueTypeFilter queryFilter = new StringValueTypeFilter();

    public DBProcessFilter() {
        pidFilter.addListener(this);
        applicationFilter.addListener(this);
        queryFilter.addListener(this);
    }

    public boolean filter(DBProcess process) {
        boolean pidFilterResult = !pidFilter.isActive() || pidFilter.filter(process.getPid());
        boolean queryFilterResult = !queryFilter.isActive() || queryFilter.filter(process.getQuery().getQueryString());
        boolean applicationFilterResult = !applicationFilter.isActive() || applicationFilter.filter(process.getQueryCaller().getApplicationName());
        return pidFilterResult && queryFilterResult && applicationFilterResult;
    }

    public void resetFilters() {
        pidFilter.reset();
        listeners.forEach(DBProcessFilterListener::dbProcessFilterChanged);
    }

    public IntegerValueTypeFilter getPidFilter() {
        return pidFilter;
    }

    public StringValueTypeFilter getQueryFilter() {
        return queryFilter;
    }

    public StringValueTypeFilter getApplicationFilter() {
        return applicationFilter;
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
