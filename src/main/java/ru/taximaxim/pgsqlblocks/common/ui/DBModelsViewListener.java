package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.action.IMenuManager;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;

public interface DBModelsViewListener {

    void dbModelsViewDidSelectController(DBController controller);

    void dbModelsViewDidCallActionToController(DBController controller);

    void dbModelsViewDidShowMenu(IMenuManager menuManager);

}
