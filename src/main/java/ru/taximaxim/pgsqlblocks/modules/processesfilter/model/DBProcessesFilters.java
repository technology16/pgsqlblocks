package ru.taximaxim.pgsqlblocks.modules.processesfilter.model;

import ru.taximaxim.pgsqlblocks.common.models.DBProcess;

public class DBProcessesFilters {

    private final DBProcessesPidFilter pidFilter = new DBProcessesPidFilter();

    public DBProcessesFilters() {

    }

    public boolean filter(DBProcess process) {
        boolean pidFilterResult = pidFilter.filter(process);
        return pidFilterResult;
    }

    public DBProcessesPidFilter getPidFilter() {
        return pidFilter;
    }
}
