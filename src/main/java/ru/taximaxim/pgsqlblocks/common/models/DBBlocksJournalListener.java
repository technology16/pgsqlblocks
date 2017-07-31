package ru.taximaxim.pgsqlblocks.common.models;


public interface DBBlocksJournalListener {

    void dbBlocksJournalDidAddProcesses();

    void dbBlocksJournalDidCloseProcesses();

}
