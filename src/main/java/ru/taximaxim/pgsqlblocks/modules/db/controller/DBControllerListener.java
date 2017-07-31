package ru.taximaxim.pgsqlblocks.modules.db.controller;

import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.modules.db.model.DBStatus;

import java.sql.SQLException;

public interface DBControllerListener {

    void dbControllerStatusChanged(DBController controller, DBStatus newStatus);

    void dbControllerDidConnect(DBController controller);

    void dbControllerWillConnect(DBController controller);

    void dbControllerConnectionFailed(DBController controller, SQLException exception);

    void dbControllerDisconnectFailed(DBController controller, SQLException exception);

    void dbControllerDidDisconnect(DBController controller);

    void dbControllerWillUpdateProcesses(DBController controller);

    void dbControllerProcessesUpdated(DBController controller);

    void dbControllerBlockedChanged(DBController controller);

    void dbControllerProcessesFilterChanged(DBController controller);

    void dbControllerBlocksJournalChanged(DBController controller);

}
