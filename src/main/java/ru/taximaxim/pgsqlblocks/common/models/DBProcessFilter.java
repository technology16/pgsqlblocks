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

    private final StringValueTypeFilter databaseFilter = new StringValueTypeFilter();

    private final StringValueTypeFilter queryFilter = new StringValueTypeFilter();

    private final StringValueTypeFilter userNameFilter = new StringValueTypeFilter();

    private final StringValueTypeFilter clientFilter = new StringValueTypeFilter();

    public DBProcessFilter() {
        pidFilter.addListener(this);
        applicationFilter.addListener(this);
        queryFilter.addListener(this);
        databaseFilter.addListener(this);
        userNameFilter.addListener(this);
        clientFilter.addListener(this);
    }

    public boolean filter(DBProcess process) {
        boolean pidFilterResult = !pidFilter.isActive() || pidFilter.filter(process.getPid());
        boolean queryFilterResult = !queryFilter.isActive() || queryFilter.filter(process.getQuery().getQueryString());
        boolean applicationFilterResult = !applicationFilter.isActive() || applicationFilter.filter(process.getQueryCaller().getApplicationName());
        boolean databaseFilterResult = !databaseFilter.isActive() || databaseFilter.filter(process.getQueryCaller().getDatabaseName());
        boolean userNameFilterResult = !userNameFilter.isActive() || userNameFilter.filter(process.getQueryCaller().getUserName());
        boolean clientFilterResult = !clientFilter.isActive() || clientFilter.filter(process.getQueryCaller().getClient());
        return pidFilterResult && queryFilterResult && applicationFilterResult && databaseFilterResult
                && userNameFilterResult && clientFilterResult;
    }

    public void resetFilters() {
        pidFilter.reset();
        databaseFilter.reset();
        queryFilter.reset();
        applicationFilter.reset();
        userNameFilter.reset();
        clientFilter.reset();
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

    public StringValueTypeFilter getDatabaseFilter() {
        return databaseFilter;
    }

    public StringValueTypeFilter getUserNameFilter() {
        return userNameFilter;
    }

    public StringValueTypeFilter getClientFilter() {
        return clientFilter;
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
