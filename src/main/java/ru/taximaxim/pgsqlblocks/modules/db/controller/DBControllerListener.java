package ru.taximaxim.pgsqlblocks.modules.db.controller;

import ru.taximaxim.pgsqlblocks.modules.db.model.DBStatus;

import java.sql.SQLException;

public interface DBControllerListener {

    void dbControllerStatusChanged(DBController controller, DBStatus newStatus);

    void dbControllerDidConnect(DBController controller);

    void dbControllerWillConnect(DBController controller);

    void dbControllerConnectionFailed(DBController controller, SQLException exception);

    void dbControllerDidDisconnect(DBController controller);

    void processesUpdated(DBController controller);

}
