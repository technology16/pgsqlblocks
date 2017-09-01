package ru.taximaxim.pgsqlblocks.common.models;

import java.util.List;

public interface DBBlocksJournalListener {

    void dbBlocksJournalDidAddProcesses();

    void dbBlocksJournalDidCloseAllProcesses();

    void dbBlocksJournalDidCloseProcesses(List<DBBlocksJournalProcess> processes);

    void dbBlocksJournalDidChangeFilters();

}
