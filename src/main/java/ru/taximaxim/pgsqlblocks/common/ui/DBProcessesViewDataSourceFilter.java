package ru.taximaxim.pgsqlblocks.common.ui;

import ru.taximaxim.pgsqlblocks.common.models.DBProcess;

import java.util.ArrayList;
import java.util.List;

public class DBProcessesViewDataSourceFilter implements TMTreeViewerDataSourceFilter<DBProcess> {

    private final List<DBProcessesViewDataSourceFilterListener> listeners = new ArrayList<>();

    @Override
    public boolean filter(DBProcess process) {
        if (isShowOnlyBlockedProcesses()) {
            return process.hasChildren();
        }
        return true;
    }

    private boolean showOnlyBlockedProcesses = false;

    public boolean isShowOnlyBlockedProcesses() {
        return showOnlyBlockedProcesses;
    }

    public void setShowOnlyBlockedProcesses(boolean showOnlyBlockedProcesses) {
        this.showOnlyBlockedProcesses = showOnlyBlockedProcesses;
        listeners.forEach(listener -> listener.dataSourceFilterShowOnlyBlockedProcessesChanged(this.showOnlyBlockedProcesses));
    }

    public void addListener(DBProcessesViewDataSourceFilterListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBProcessesViewDataSourceFilterListener listener) {
        listeners.remove(listener);
    }


}
