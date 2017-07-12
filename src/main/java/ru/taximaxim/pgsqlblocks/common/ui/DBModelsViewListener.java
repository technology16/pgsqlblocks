package ru.taximaxim.pgsqlblocks.common.ui;

import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;

public interface DBModelsViewListener {

    void didSelectController(DBController controller);

    void didCallActionToController(DBController controller);

}
