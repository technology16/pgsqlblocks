package ru.taximaxim.pgsqlblocks.modules.processesfilter.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.modules.processesfilter.view.DBProcessesFiltersView;

public class DBProcessesFiltersController {

    private DBController dbController;

    private DBProcessesFiltersView view;

    public DBController getDbController() {
        return dbController;
    }

    public void setDbController(DBController dbController) {
        this.dbController = dbController;
    }

    public void presentInView(Composite parentComposite) {
        view = new DBProcessesFiltersView(parentComposite, SWT.NONE);
    }

}
