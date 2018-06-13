/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.common.models;

import ru.taximaxim.pgsqlblocks.common.Filter;
import ru.taximaxim.pgsqlblocks.common.FilterListener;
import ru.taximaxim.pgsqlblocks.common.IntegerValueTypeFilter;
import ru.taximaxim.pgsqlblocks.common.StringValueTypeFilter;

import java.util.ArrayList;
import java.util.List;

public class DBProcessFilter implements FilterListener {

    private boolean enabled;

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
        if (!enabled) {
            return true;
        }
        return filterProcess(process);
    }

    private boolean filterProcess(DBProcess process) {
        boolean pidFilterResult = !pidFilter.isActive() || pidFilter.filter(process.getPid());
        boolean queryFilterResult = !queryFilter.isActive() || queryFilter.filter(process.getQuery().getQueryString());
        boolean applicationFilterResult = !applicationFilter.isActive() || applicationFilter.filter(process.getQueryCaller().getApplicationName());
        boolean databaseFilterResult = !databaseFilter.isActive() || databaseFilter.filter(process.getQueryCaller().getDatabaseName());
        boolean userNameFilterResult = !userNameFilter.isActive() || userNameFilter.filter(process.getQueryCaller().getUserName());
        boolean clientFilterResult = !clientFilter.isActive() || clientFilter.filter(process.getQueryCaller().getClient());
        boolean result = pidFilterResult && queryFilterResult && applicationFilterResult && databaseFilterResult
                && userNameFilterResult && clientFilterResult;
        if (!result && process.hasChildren()) {
            for (DBProcess childProcess : process.getChildren()) {
                boolean childFilterResult = filterProcess(childProcess);
                if (childFilterResult) {
                    return true;
                }
            }
        }
        return result;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            listeners.forEach(DBProcessFilterListener::dbProcessFilterChanged);
        }
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
