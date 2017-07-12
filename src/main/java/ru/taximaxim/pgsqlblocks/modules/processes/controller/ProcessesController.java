package ru.taximaxim.pgsqlblocks.modules.processes.controller;


import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import ru.taximaxim.pgsqlblocks.common.DBModelsProvider;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.common.ui.DBModelsView;
import ru.taximaxim.pgsqlblocks.common.ui.DBModelsViewListener;
import ru.taximaxim.pgsqlblocks.common.ui.DBProcessesView;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBControllerListener;
import ru.taximaxim.pgsqlblocks.modules.db.model.DBStatus;
import ru.taximaxim.pgsqlblocks.modules.processes.view.ProcessesView;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProcessesController implements DBControllerListener, DBModelsViewListener {

    private static final Logger LOG = Logger.getLogger(ProcessesController.class);

    private Settings settings = Settings.getInstance();
    private ResourceBundle resourceBundle = settings.getResourceBundle();

    private ProcessesView view;

    private DBModelsView dbModelsView;
    private DBProcessesView dbProcessesView;

    private DBModelsProvider dbModelsProvider;

    private List<DBController> dbControllers = new ArrayList<>();

    public ProcessesController(DBModelsProvider dbModelsProvider) {
        this.dbModelsProvider = dbModelsProvider;
    }

    public void setView(ProcessesView view) {
        this.view = view;
    }

    public void load() {
        dbModelsView = new DBModelsView(view.getLeftPanelComposite(), SWT.NONE);
        dbModelsView.addListener(this);
        dbProcessesView = new DBProcessesView(view.getRightPanelComposite(), SWT.NONE);
        getDatabases();
    }

    private void getDatabases() {
        List<DBModel> dbModels = dbModelsProvider.get();
        dbModels.forEach(this::addDatabase);
    }

    private void addDatabase(DBModel dbModel) {
        DBController controller = new DBController(dbModel);
        controller.addListener(this);
        dbControllers.add(controller);
        dbModelsView.getTreeViewer().add(controller);
    }

    private void saveDatabases() {
        List<DBModel> models = dbControllers.stream().map(DBController::getModel).collect(Collectors.toList());
        dbModelsProvider.save(models);
    }

    @Override
    public void dbControllerStatusChanged(DBController controller, DBStatus newStatus) {
        dbModelsView.getTreeViewer().refresh(controller, true, true);
    }

    @Override
    public void dbControllerDidConnect(DBController controller) {
        if (settings.isAutoUpdate()) {
            // TODO запускаем обновление запросов бд
        }
    }

    @Override
    public void dbControllerWillConnect(DBController controller) {
        LOG.info(MessageFormat.format(resourceBundle.getString("db_connecting"), controller.getModel().getName()));
    }

    @Override
    public void dbControllerConnectionFailed(DBController controller, SQLException exception) {
        LOG.error(controller.getModel().getName() + " " + exception.getMessage(), exception);
    }

    @Override
    public void dbControllerDidDisconnect(DBController controller) {

    }

    @Override
    public void processesUpdated(DBController controller) {

    }

    @Override
    public void didSelectController(DBController controller) {

    }

    @Override
    public void didCallActionToController(DBController controller) {
        if (!controller.isConnected()) {
            controller.connect();
        } else {
            controller.disconnect();
        }
    }
}
