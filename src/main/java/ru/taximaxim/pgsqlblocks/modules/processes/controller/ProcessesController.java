package ru.taximaxim.pgsqlblocks.modules.processes.controller;


import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.PgSqlBlocks;
import ru.taximaxim.pgsqlblocks.common.DBModelsProvider;
import ru.taximaxim.pgsqlblocks.common.FilterCondition;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.common.ui.*;
import ru.taximaxim.pgsqlblocks.dialogs.AddDatabaseDialog;
import ru.taximaxim.pgsqlblocks.dialogs.EditDatabaseDialog;
import ru.taximaxim.pgsqlblocks.dialogs.SettingsDialog;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBControllerListener;
import ru.taximaxim.pgsqlblocks.modules.db.model.DBStatus;
import ru.taximaxim.pgsqlblocks.modules.processes.view.ProcessesView;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;
import ru.taximaxim.pgsqlblocks.utils.Images;
import ru.taximaxim.pgsqlblocks.utils.Settings;
import ru.taximaxim.pgsqlblocks.utils.SettingsListener;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProcessesController implements DBControllerListener, DBModelsViewListener, SettingsListener,
        DBProcessesViewDataSourceFilterListener, DBProcessesFiltersViewListener,
        TMTreeViewerSortColumnSelectionListener, DBProcessInfoViewListener {

    private static final Logger LOG = Logger.getLogger(ProcessesController.class);

    private Settings settings = Settings.getInstance();
    private ResourceBundle resourceBundle = settings.getResourceBundle();

    private ProcessesView view;

    private DBModelsView dbModelsView;
    private DBProcessesView dbProcessesView;
    private DBProcessesFiltersView dbProcessesFiltersView;
    private DBProcessInfoView dbProcessInfoView;

    private DBProcessesView dbBlocksJournalView;

    private final DBProcessesViewDataSourceFilter dbProcessesViewDataSourceFilter = new DBProcessesViewDataSourceFilter();

    private TabFolder tabFolder;

    private ToolItem addDatabaseToolItem;
    private ToolItem deleteDatabaseToolItem;
    private ToolItem editDatabaseToolItem;
    private ToolItem connectDatabaseToolItem;
    private ToolItem disconnectDatabaseToolItem;
    private ToolItem updateProcessesToolItem;
    private ToolItem autoUpdateToolItem;
    private ToolItem showOnlyBlockedProcessesToolItem;
    private ToolItem toggleVisibilityProcessesFilterPanelToolItem;

    private DBModelsProvider dbModelsProvider;

    private List<DBController> dbControllers = new ArrayList<>();

    private DBProcess selectedProcess;

    public ProcessesController(DBModelsProvider dbModelsProvider) {
        this.dbModelsProvider = dbModelsProvider;
        settings.addListener(this);
    }

    public void setView(ProcessesView view) {
        this.view = view;
    }

    public void load() {
        createToolItems();

        dbModelsView = new DBModelsView(view.getLeftPanelComposite(), SWT.NONE);
        dbModelsView.getTreeViewer().setInput(dbControllers);
        dbModelsView.addListener(this);

        dbProcessesViewDataSourceFilter.addListener(this);

        tabFolder = new TabFolder(view.getRightPanelComposite(), SWT.NONE);

        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createProcessesTab();
        createBlocksJournalTab();

        loadDatabases();

        dbControllers.stream().filter(DBController::isEnabledAutoConnection).forEach(DBController::connect);

        DriverManager.setLoginTimeout(settings.getLoginTimeout());
    }

    private void createProcessesTab() {
        TabItem processesTabItem = new TabItem(tabFolder, SWT.BORDER);
        processesTabItem.setText(resourceBundle.getString("current_activity"));

        Composite processesViewComposite = new Composite(tabFolder, SWT.NONE);
        processesViewComposite.setLayout(new GridLayout());

        dbProcessesFiltersView = new DBProcessesFiltersView(processesViewComposite, SWT.NONE);
        dbProcessesFiltersView.addListener(this);
        dbProcessesFiltersView.hide();

        dbProcessesView = new DBProcessesView(processesViewComposite, SWT.NONE);
        DBProcessesViewDataSource dbProcessesViewDataSource = new DBProcessesViewDataSource(resourceBundle, dbProcessesViewDataSourceFilter);
        dbProcessesView.getTreeViewer().setDataSource(dbProcessesViewDataSource);
        dbProcessesView.getTreeViewer().addSortColumnSelectionListener(this);
        dbProcessesView.getTreeViewer().addSelectionChangedListener(this::dbProcessesViewSelectionChanged);

        dbProcessInfoView = new DBProcessInfoView(processesViewComposite, SWT.NONE);
        dbProcessInfoView.addListener(this);
        dbProcessInfoView.hide();

        processesTabItem.setControl(processesViewComposite);
    }

    private void createBlocksJournalTab() {
        TabItem blocksJournalTabItem = new TabItem(tabFolder, SWT.NONE);
        blocksJournalTabItem.setText(resourceBundle.getString("blocks_journal"));

        Composite dbBlocksJournalViewComposite = new Composite(tabFolder, SWT.NONE);
        dbBlocksJournalViewComposite.setLayout(new GridLayout());

        dbBlocksJournalView = new DBProcessesView(dbBlocksJournalViewComposite, SWT.NONE);
        dbBlocksJournalView.getTreeViewer().setDataSource(new DBBlocksJournalViewDataSource(resourceBundle));
        blocksJournalTabItem.setControl(dbBlocksJournalViewComposite);
    }

    private void createToolItems() {
        addDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        addDatabaseToolItem.setImage(ImageUtils.getImage(Images.ADD_DATABASE));
        addDatabaseToolItem.setToolTipText(Images.ADD_DATABASE.getDescription(resourceBundle));
        addDatabaseToolItem.addListener(SWT.Selection, event -> openAddNewDatabaseDialog());

        deleteDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        deleteDatabaseToolItem.setImage(ImageUtils.getImage(Images.DELETE_DATABASE));
        deleteDatabaseToolItem.setToolTipText(Images.DELETE_DATABASE.getDescription(resourceBundle));
        deleteDatabaseToolItem.setEnabled(false);
        deleteDatabaseToolItem.addListener(SWT.Selection, event -> deleteSelectedDatabase());

        editDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        editDatabaseToolItem.setImage(ImageUtils.getImage(Images.EDIT_DATABASE));
        editDatabaseToolItem.setToolTipText(Images.EDIT_DATABASE.getDescription(resourceBundle));
        editDatabaseToolItem.setEnabled(false);
        editDatabaseToolItem.addListener(SWT.Selection, event -> openEditSelectedDatabaseDialog());

        connectDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        connectDatabaseToolItem.setImage(ImageUtils.getImage(Images.CONNECT_DATABASE));
        connectDatabaseToolItem.setToolTipText(Images.CONNECT_DATABASE.getDescription(resourceBundle));
        connectDatabaseToolItem.setEnabled(false);
        connectDatabaseToolItem.addListener(SWT.Selection, event -> connectToSelectedDatabase());

        disconnectDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        disconnectDatabaseToolItem.setImage(ImageUtils.getImage(Images.DISCONNECT_DATABASE));
        disconnectDatabaseToolItem.setToolTipText(Images.DISCONNECT_DATABASE.getDescription(resourceBundle));
        disconnectDatabaseToolItem.setEnabled(false);
        disconnectDatabaseToolItem.addListener(SWT.Selection, event -> disconnectSelectedDatabase());

        new ToolItem(view.getToolBar(), SWT.SEPARATOR);

        updateProcessesToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        updateProcessesToolItem.setImage(ImageUtils.getImage(Images.UPDATE));
        updateProcessesToolItem.setToolTipText(Images.UPDATE.getDescription(resourceBundle));
        updateProcessesToolItem.addListener(SWT.Selection, event -> updateProcessesInSelectedDatabase());

        autoUpdateToolItem = new ToolItem(view.getToolBar(), SWT.CHECK);
        autoUpdateToolItem.setImage(ImageUtils.getImage(Images.AUTOUPDATE));
        autoUpdateToolItem.setToolTipText(Images.AUTOUPDATE.getDescription(resourceBundle));
        autoUpdateToolItem.addListener(SWT.Selection, event -> setAutoUpdate(autoUpdateToolItem.getSelection()));
        autoUpdateToolItem.setSelection(settings.isAutoUpdate());

        new ToolItem(view.getToolBar(), SWT.SEPARATOR);

        showOnlyBlockedProcessesToolItem = new ToolItem(view.getToolBar(), SWT.CHECK);
        showOnlyBlockedProcessesToolItem.setImage(ImageUtils.getImage(Images.VIEW_ONLY_BLOCKED));
        showOnlyBlockedProcessesToolItem.setToolTipText(Images.VIEW_ONLY_BLOCKED.getDescription(resourceBundle));
        showOnlyBlockedProcessesToolItem.addListener(SWT.Selection, event ->
                dbProcessesViewDataSourceFilter.setShowOnlyBlockedProcesses(showOnlyBlockedProcessesToolItem.getSelection()));

        toggleVisibilityProcessesFilterPanelToolItem = new ToolItem(view.getToolBar(), SWT.CHECK);
        toggleVisibilityProcessesFilterPanelToolItem.setImage(ImageUtils.getImage(Images.FILTER));
        toggleVisibilityProcessesFilterPanelToolItem.setToolTipText(Images.FILTER.getDescription(resourceBundle));
        toggleVisibilityProcessesFilterPanelToolItem.addListener(SWT.Selection, event ->
                setProcessesFilterViewVisibility(toggleVisibilityProcessesFilterPanelToolItem.getSelection()));

        new ToolItem(view.getToolBar(), SWT.SEPARATOR);

        ToolItem showSettingsDialogToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        showSettingsDialogToolItem.setImage(ImageUtils.getImage(Images.SETTINGS));
        showSettingsDialogToolItem.setToolTipText(Images.SETTINGS.getDescription(resourceBundle));
        showSettingsDialogToolItem.addListener(SWT.Selection, event -> showSettingsDialog());

        ToolItem toggleLogsPanelVisibilityToolItem = new ToolItem(view.getToolBar(), SWT.CHECK);
        toggleLogsPanelVisibilityToolItem.setImage(ImageUtils.getImage(Images.SHOW_LOG_PANEL));
        toggleLogsPanelVisibilityToolItem.setToolTipText(Images.SHOW_LOG_PANEL.getDescription(resourceBundle));
        toggleLogsPanelVisibilityToolItem.setSelection(true);
        toggleLogsPanelVisibilityToolItem.addListener(SWT.Selection, event -> toggleLogsPanelVisibility(toggleLogsPanelVisibilityToolItem));
    }

    private void changeToolItemsStateForController(DBController controller) {
        boolean isEnabled = controller != null && !controller.isConnected();
        dbProcessInfoView.getToolBar().setEnabled(!isEnabled);
        toggleVisibilityProcessesFilterPanelToolItem.setEnabled(controller != null);
        deleteDatabaseToolItem.setEnabled(isEnabled);
        editDatabaseToolItem.setEnabled(isEnabled);
        connectDatabaseToolItem.setEnabled(isEnabled);
        disconnectDatabaseToolItem.setEnabled(controller != null && !isEnabled);
    }

    private void toggleLogsPanelVisibility(ToolItem toolItem) {
        boolean isShow = toolItem.getSelection();
        if (isShow) {
            toolItem.setImage(ImageUtils.getImage(Images.SHOW_LOG_PANEL));
            toolItem.setToolTipText(Images.SHOW_LOG_PANEL.getDescription(resourceBundle));
            PgSqlBlocks.getInstance().getApplicationController().getApplicationView().showBottomPanel();
        } else {
            toolItem.setImage(ImageUtils.getImage(Images.HIDE_LOG_PANEL));
            toolItem.setToolTipText(Images.HIDE_LOG_PANEL.getDescription(resourceBundle));
            PgSqlBlocks.getInstance().getApplicationController().getApplicationView().hideBottomPanel();
        }
    }

    private void loadDatabases() {
        List<DBModel> dbModels = dbModelsProvider.get();
        dbModels.forEach(this::addDatabase);
        dbModelsView.getTreeViewer().refresh();
    }

    private void addDatabase(DBModel dbModel) {
        addDatabase(dbModel, dbControllers.size());
    }

    private void addDatabase(DBModel dbModel, int index) {
        DBController controller = new DBController(dbModel);
        controller.addListener(this);
        dbControllers.add(index, controller);
    }

    private void deleteDatabase(DBController dbController) {
        dbController.removeListener(this);
        dbController.shutdown();
        dbControllers.remove(dbController);
        changeToolItemsStateForController(null);
        dbProcessesView.getTreeViewer().setInput(null);
        dbBlocksJournalView.getTreeViewer().setInput(null);
        toggleVisibilityProcessesFilterPanelToolItem.setSelection(false);
        dbProcessesFiltersView.hide();
        dbProcessesFiltersView.fillViewForController(null);
    }

    private void saveDatabases() {
        List<DBModel> models = dbControllers.stream().map(DBController::getModel).collect(Collectors.toList());
        dbModelsProvider.save(models);
    }

    private void openAddNewDatabaseDialog() {
        List<String> reservedConnectionNames = dbControllers.stream()
                .map(DBController::getModel)
                .map(DBModel::getName)
                .collect(Collectors.toList());
        AddDatabaseDialog addDatabaseDialog = new AddDatabaseDialog(view.getShell(), reservedConnectionNames);
        if (addDatabaseDialog.open() == Window.OK) {
            addDatabase(addDatabaseDialog.getCreatedModel());
            dbModelsView.getTreeViewer().refresh();
            saveDatabases();
        }
    }

    private void editDatabase(DBModel oldModel, DBModel newModel) {
        Optional<DBController> opt = dbControllers.stream().filter(dbc -> dbc.getModel().equals(oldModel)).findFirst();
        opt.ifPresent(controller -> {
            controller.setModel(newModel);
            dbModelsView.getTreeViewer().refresh(true, true);
            saveDatabases();
            if (controller.isEnabledAutoConnection()) {
                controller.connect();
            }
        });
    }

    private void openEditSelectedDatabaseDialog() {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        List<String> reservedConnectionNames = dbControllers.stream()
                .map(DBController::getModel)
                .map(DBModel::getName)
                .collect(Collectors.toList());
        EditDatabaseDialog editDatabaseDialog = new EditDatabaseDialog(view.getShell(), reservedConnectionNames, selectedController.getModel());
        if (editDatabaseDialog.open() == Window.OK) {
            editDatabase(editDatabaseDialog.getEditedModel(), editDatabaseDialog.getCreatedModel());
        }
    }

    private void deleteSelectedDatabase() {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        deleteDatabase(selectedController);
        dbModelsView.getTreeViewer().refresh();
        saveDatabases();
    }

    private void connectToSelectedDatabase() {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() != null) {
            DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
            selectedController.connect();
        }
    }

    private void disconnectSelectedDatabase() {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() != null) {
            DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
            selectedController.disconnect();
        }
    }

    private void updateProcessesInSelectedDatabase() {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() != null) {
            DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
            selectedController.updateProcesses();
        }
    }

    private void setAutoUpdate(boolean autoUpdate) {
        settings.setAutoUpdate(autoUpdate);
    }

    public void setProcessesFilterViewVisibility(boolean isVisible) {
        if (isVisible) {
            dbProcessesFiltersView.show();
        } else {
            dbProcessesFiltersView.hide();
        }
    }

    private void showDBBlockedMessageInTray(DBController controller) {
        view.getDisplay().asyncExec(() -> {
            if (PgSqlBlocks.getInstance().getApplicationController().getApplicationView().trayIsAvailable()) {
                Tray tray = PgSqlBlocks.getInstance().getApplicationController().getApplicationView().getTray();
                TrayItem trayItem = tray.getItem(0);
                trayItem.setImage(ImageUtils.getImage(Images.BLOCKED));
                ToolTip toolTip = trayItem.getToolTip();
                toolTip.setMessage(MessageFormat.format(resourceBundle.getString("db_has_lock"), controller.getModel().getName()));
                toolTip.setVisible(true);
            } else {
                LOG.error("Tray is not available");
            }
        });
    }

    private void hideTrayMessageIfAllDatabasesUnblocked() {
        view.getDisplay().asyncExec(() -> {
            if (PgSqlBlocks.getInstance().getApplicationController().getApplicationView().trayIsAvailable()) {
                if (!dbControllers.stream().anyMatch(DBController::isBlocked)) {
                    Tray tray = PgSqlBlocks.getInstance().getApplicationController().getApplicationView().getTray();
                    TrayItem trayItem = tray.getItem(0);
                    trayItem.setImage(ImageUtils.getImage(Images.UNBLOCKED));
                    ToolTip toolTip = trayItem.getToolTip();
                    toolTip.setVisible(false);
                }
            }
        });
    }

    private void showSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog(view.getShell(), settings);
        settingsDialog.open();
    }

    public void close() {
        dbControllers.forEach(DBController::shutdown);
        settings.removeListener(this);
    }

    @Override
    public void dbControllerStatusChanged(DBController controller, DBStatus newStatus) {
        dbModelsView.getTreeViewer().refresh(controller, true, true);
    }

    @Override
    public void dbControllerDidConnect(DBController controller) {
        if (settings.isAutoUpdate()) {
            controller.startProcessesUpdater(settings.getUpdatePeriod());
        }
        changeToolItemsStateForController(controller);
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
    public void dbControllerDisconnectFailed(DBController controller, SQLException exception) {
        LOG.error(controller.getModel().getName() + " " + exception.getMessage(), exception);
    }

    @Override
    public void dbControllerDidDisconnect(DBController controller) {
        LOG.info(MessageFormat.format(resourceBundle.getString("db_disconnected"),controller.getModel().getName()));
        changeToolItemsStateForController(controller);
    }

    @Override
    public void dbControllerWillUpdateProcesses(DBController controller) {
        LOG.info(MessageFormat.format(resourceBundle.getString("db_updating"), controller.getModel().getName()));
    }

    @Override
    public void dbControllerProcessesUpdated(DBController controller) {
        view.getDisplay().asyncExec(() -> {
            if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() != null) {
                DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
                if (controller.equals(selectedController)) {
                    dbProcessesView.getTreeViewer().refresh();
                }
            }
        });
    }

    @Override
    public void dbControllerBlockedChanged(DBController controller) {
        view.getDisplay().asyncExec(() -> dbModelsView.getTreeViewer().refresh(controller));
        if (controller.isBlocked()) {
            showDBBlockedMessageInTray(controller);
        } else {
            hideTrayMessageIfAllDatabasesUnblocked();
        }
    }

    @Override
    public void dbControllerProcessesFilterChanged(DBController controller) {
        dbProcessesView.getTreeViewer().refresh();
    }

    @Override
    public void dbControllerBlocksJournalChanged(DBController controller) {
        view.getDisplay().asyncExec(() -> {
            if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() != null) {
                DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
                if (controller.equals(selectedController)) {
                    dbBlocksJournalView.getTreeViewer().refresh();
                }
            }
        });
    }

    @Override
    public void didSelectController(DBController controller) {
        dbProcessesView.getTreeViewer().setInput(controller.getFilteredProcesses());
        dbBlocksJournalView.getTreeViewer().setInput(controller.getBlocksJournal().getProcesses());
        changeToolItemsStateForController(controller);
        dbProcessesFiltersView.fillViewForController(controller);
    }

    @Override
    public void didCallActionToController(DBController controller) {
        if (!controller.isConnected()) {
            controller.connect();
        } else {
            controller.disconnect();
        }
    }

    @Override
    public void settingsUpdatePeriodChanged(int updatePeriod) {
        if (settings.isAutoUpdate()) {
            dbControllers.stream().filter(DBController::isConnected).forEach(dbc -> dbc.startProcessesUpdater(settings.getUpdatePeriod()));
        }
    }

    @Override
    public void settingsShowIdleChanged(boolean isShowIdle) {
        if (settings.isAutoUpdate()) {
            dbControllers.stream().filter(DBController::isConnected).forEach(dbc -> dbc.startProcessesUpdater(settings.getUpdatePeriod()));
        }
    }

    @Override
    public void settingsAutoUpdateChanged(boolean isAutoUpdate) {
        if (isAutoUpdate) {
            dbControllers.stream().filter(DBController::isConnected).forEach(dbc -> dbc.startProcessesUpdater(settings.getUpdatePeriod()));
        } else {
            dbControllers.stream().filter(DBController::isConnected).forEach(DBController::stopProcessesUpdater);
        }
    }

    @Override
    public void dataSourceFilterShowOnlyBlockedProcessesChanged(boolean showOnlyBlockedProcesses) {
        dbProcessesView.getTreeViewer().refresh();
    }


    @Override
    public void processesFiltersViewPidFilterConditionChanged(FilterCondition condition) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getPidFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewPidFilterValueChanged(Integer value) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getPidFilter().setValue(value);

    }

    @Override
    public void processesFiltersViewQueryFilterConditionChanged(FilterCondition condition) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getQueryFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewQueryFilterValueChanged(String value) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getQueryFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewApplicationFilterConditionChanged(FilterCondition condition) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getApplicationFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewApplicationFilterValueChanged(String value) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getApplicationFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewDatabaseFilterConditionChanged(FilterCondition condition) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getDatabaseFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewDatabaseFilterValueChanged(String value) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getDatabaseFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewUserNameFilterConditionChanged(FilterCondition condition) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getUserNameFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewUserNameFilterValueChanged(String value) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getUserNameFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewClientFilterConditionChanged(FilterCondition condition) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getClientFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewClientFilterValueChanged(String value) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().getClientFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewIncludeBlockedValueChanged(boolean includeBlocked) {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        selectedController.getProcessesFilters().setIncludeBlockedProcessesWhenFiltering(includeBlocked);
    }

    @Override
    public void didSelectSortColumn(TreeColumn column, int columnIndex, int sortDirection) {
        dbProcessesView.getTreeViewer().setComparator(new DBProcessesViewComparator(columnIndex, sortDirection));
    }

    private void dbProcessesViewSelectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection.isEmpty()) {
            selectedProcess = null;
            dbProcessInfoView.hide();
        } else {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            DBProcess process = (DBProcess)structuredSelection.getFirstElement();
            selectedProcess = process;
            dbProcessInfoView.show(process);
        }
    }

    @Override
    public void dbProcessInfoViewTerminateProcessToolItemClicked() {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        if (selectedProcess != null) {
            final int processPid = selectedProcess.getPid();
            if (settings.isConfirmRequired() && !MessageDialog.openQuestion(view.getShell(), resourceBundle.getString("confirm_action"),
                    MessageFormat.format(resourceBundle.getString("kill_process_confirm_message"), processPid))) {
                return;
            }
            try {
                boolean result = selectedController.terminateProcessWithPid(processPid);
                if (result) {
                    LOG.info(MessageFormat.format(resourceBundle.getString("process_terminated"), selectedController.getModel().getName(), processPid));
                    selectedController.updateProcesses();
                } else {
                    LOG.info(MessageFormat.format(resourceBundle.getString("process_not_terminated"), selectedController.getModel().getName(), processPid));
                }
            } catch (SQLException exception) {
                LOG.error(selectedController.getModel().getName() + " " + exception.getMessage(), exception);
                LOG.info(MessageFormat.format(resourceBundle.getString("process_not_terminated"), selectedController.getModel().getName(), processPid));
            }
        }
    }

    @Override
    public void dbProcessInfoViewCancelProcessToolItemClicked() {
        if (dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTreeViewer().getStructuredSelection().getFirstElement();
        if (selectedProcess != null) {
            final int processPid = selectedProcess.getPid();
            if (settings.isConfirmRequired() && !MessageDialog.openQuestion(view.getShell(), resourceBundle.getString("confirm_action"),
                    MessageFormat.format(resourceBundle.getString("cancel_process_confirm_message"), processPid))) {
                return;
            }
            try {
                boolean result = selectedController.cancelProcessWithPid(processPid);
                if (result) {
                    LOG.info(MessageFormat.format(resourceBundle.getString("process_cancelled"), selectedController.getModel().getName(), processPid));
                    selectedController.updateProcesses();
                } else {
                    LOG.info(MessageFormat.format(resourceBundle.getString("process_not_cancelled"), selectedController.getModel().getName(), processPid));
                }
            } catch (SQLException exception) {
                LOG.error(selectedController.getModel().getName() + " " + exception.getMessage(), exception);
                LOG.info(MessageFormat.format(resourceBundle.getString("process_not_cancelled"), selectedController.getModel().getName(), processPid));
            }
        }
    }
}
