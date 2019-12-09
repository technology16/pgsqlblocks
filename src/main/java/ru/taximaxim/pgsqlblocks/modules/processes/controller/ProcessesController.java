/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
 * %
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.modules.processes.controller;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import ru.taximaxim.pgsqlblocks.PgSqlBlocks;
import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.common.ui.DBBlocksJournalViewDataSource;
import ru.taximaxim.pgsqlblocks.common.ui.DBModelsView;
import ru.taximaxim.pgsqlblocks.common.ui.DBModelsViewListener;
import ru.taximaxim.pgsqlblocks.common.ui.DBProcessInfoView;
import ru.taximaxim.pgsqlblocks.common.ui.DBProcessInfoViewListener;
import ru.taximaxim.pgsqlblocks.common.ui.DBProcessesViewDataSource;
import ru.taximaxim.pgsqlblocks.dialogs.AddDatabaseDialog;
import ru.taximaxim.pgsqlblocks.dialogs.DBProcessInfoDialog;
import ru.taximaxim.pgsqlblocks.dialogs.EditDatabaseDialog;
import ru.taximaxim.pgsqlblocks.dialogs.PasswordDialog;
import ru.taximaxim.pgsqlblocks.dialogs.SettingsDialog;
import ru.taximaxim.pgsqlblocks.modules.blocksjournal.view.BlocksJournalView;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBControllerListener;
import ru.taximaxim.pgsqlblocks.modules.db.controller.UserInputPasswordProvider;
import ru.taximaxim.pgsqlblocks.modules.db.model.DBStatus;
import ru.taximaxim.pgsqlblocks.modules.processes.view.ProcessesView;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;
import ru.taximaxim.pgsqlblocks.utils.Images;
import ru.taximaxim.pgsqlblocks.utils.Settings;
import ru.taximaxim.pgsqlblocks.utils.SettingsListener;
import ru.taximaxim.pgsqlblocks.utils.SupportedVersion;
import ru.taximaxim.pgsqlblocks.utils.UserCancelException;
import ru.taximaxim.pgsqlblocks.xmlstore.ColumnLayoutsXmlStore;
import ru.taximaxim.pgsqlblocks.xmlstore.DBModelsXmlStore;
import ru.taximaxim.treeviewer.ExtendedTreeViewer;

public class ProcessesController implements DBControllerListener, UserInputPasswordProvider, DBModelsViewListener,
SettingsListener, DBProcessInfoViewListener {

    private static final Logger LOG = Logger.getLogger(ProcessesController.class);

    private final Settings settings;
    private final ResourceBundle resourceBundle;

    private ProcessesView view;
    private DBModelsView dbModelsView;
    private ExtendedTreeViewer<DBProcess> dbProcessView;
    private DBProcessInfoView dbProcessInfoView;

    private ExtendedTreeViewer<DBProcess> dbBlocksJournalView;

    private DBProcessInfoView dbBlocksJournalProcessInfoView;

    private TabFolder tabFolder;

    private ToolItem addDatabaseToolItem;
    private ToolItem deleteDatabaseToolItem;
    private ToolItem editDatabaseToolItem;
    private ToolItem connectDatabaseToolItem;
    private ToolItem disconnectDatabaseToolItem;
    private ToolItem autoUpdateToolItem;
    private ToolItem showOnlyBlockedProcessesToolItem;

    private final DBModelsXmlStore store = new DBModelsXmlStore();

    private OnlyBlockedFilter onlyBlockedFilter;

    private final List<DBController> dbControllers = new ArrayList<>();

    private List<DBProcess> selectedProcesses = new ArrayList<>();

    public ProcessesController(Settings settings) {
        this.settings = settings;
        this.resourceBundle = settings.getResourceBundle();
        settings.addListener(this);
    }

    public void setView(ProcessesView view) {
        this.view = view;
    }

    public void load() {
        createToolItems();

        dbModelsView = new DBModelsView(resourceBundle, view.getLeftPanelComposite(), SWT.NONE);
        dbModelsView.getTableViewer().setInput(dbControllers);
        dbModelsView.addListener(this);
        tabFolder = new TabFolder(view.getRightPanelComposite(), SWT.NONE);

        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createProcessesTab();
        createBlocksJournalTab();

        loadDatabases();

        dbControllers.stream().filter(DBController::isEnabledAutoConnection).forEach(DBController::connectAsync);
    }

    private void createProcessesTab() {
        TabItem processesTabItem = new TabItem(tabFolder, SWT.BORDER);
        processesTabItem.setText(l10n("current_activity"));

        SashForm processesViewSash = new SashForm(tabFolder, SWT.VERTICAL);
        Composite processesViewComposite = new Composite(processesViewSash, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        processesViewComposite.setLayout(gl);
        processesViewSash.setLayout(gl);
        processesViewSash.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        DBProcessesViewDataSource dbProcessesViewDataSource = new DBProcessesViewDataSource(resourceBundle);
        dbProcessView = new ExtendedTreeViewer<>(processesViewComposite, SWT.NONE,
                null, dbProcessesViewDataSource, settings.getLocale(),
                new ColumnLayoutsXmlStore("dbProcess.xml"));
        dbProcessView.getTreeViewer().addSelectionChangedListener(this::dbProcessesViewSelectionChanged);
        dbProcessView.setUpdateButtonAction(this::updateProcessesInSelectedDatabase);
        dbProcessView.getTreeViewer().getTree().addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                IStructuredSelection selection = dbProcessView.getTreeViewer().getStructuredSelection();
                if (!selection.isEmpty()) {
                    openProcessInfoDialog((DBProcess) selection.getFirstElement());
                }
            }
        });

        dbProcessInfoView = new DBProcessInfoView(resourceBundle, processesViewSash, SWT.NONE);
        dbProcessInfoView.addListener(this);
        dbProcessInfoView.hide();

        processesTabItem.setControl(processesViewSash);
    }

    private void openProcessInfoDialog(DBProcess dbProcess) {
        DBProcessInfoDialog dbProcessInfoDialog = new DBProcessInfoDialog(resourceBundle, view.getShell(), dbProcess, false);
        dbProcessInfoDialog.setProcessInfoListener(new DBProcessInfoDialog.ProcessInfoListener() {
            @Override
            public void terminateButtonClick() {
                terminateButtonClicked(dbProcess);
            }

            @Override
            public void cancelButtonClick() {
                cancelButtonClicked(dbProcess);
            }
        });
        dbProcessInfoDialog.open();
    }

    private void cancelButtonClicked(DBProcess dbProcess) {
        Object selectedController = dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
        if (selectedController == null || dbProcess == null) {
            return;
        }
        List<Integer> pidProcessesList = Collections.singletonList(dbProcess.getPid());
        cancelProcessesByPid((DBController) selectedController, pidProcessesList);
    }

    private void terminateButtonClicked(DBProcess dbProcess) {
        Object selectedController = dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
        if (selectedController == null || dbProcess == null) {
            return;
        }
        List<Integer> pidProcessesList = Collections.singletonList(dbProcess.getPid());
        terminateProcessesByPid((DBController) selectedController, pidProcessesList);
    }

    private void createBlocksJournalTab() {
        TabItem blocksJournalTabItem = new TabItem(tabFolder, SWT.NONE);
        blocksJournalTabItem.setText(l10n("blocks_journal"));

        SashForm dbBlocksJournalViewSash = new SashForm(tabFolder, SWT.VERTICAL);
        Composite dbBlocksJournalViewComposite = new Composite(dbBlocksJournalViewSash, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        dbBlocksJournalViewSash.setLayout(gl);
        dbBlocksJournalViewSash.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        dbBlocksJournalViewComposite.setLayout(gl);

        DBBlocksJournalViewDataSource dbBlocksJournalViewDataSource = new DBBlocksJournalViewDataSource(resourceBundle);
        dbBlocksJournalView = new ExtendedTreeViewer<>(dbBlocksJournalViewComposite,
                SWT.NONE, null, dbBlocksJournalViewDataSource, settings.getLocale(),
                new ColumnLayoutsXmlStore("dbBlocksJournal"));
        dbBlocksJournalView.getTreeViewer().addSelectionChangedListener(this::dbBlocksJournalViewSelectionChanged);
        dbBlocksJournalView.getTreeViewer().getTree().addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                IStructuredSelection structuredSelection = dbProcessView.getTreeViewer().getStructuredSelection();
                DBProcess process = (DBProcess) structuredSelection.getFirstElement();
                openProcessInfoDialog(process);
            }
        });
        dbBlocksJournalView.setUpdateButtonAction(this::updateProcessesInSelectedDatabase);

        dbBlocksJournalProcessInfoView = new DBProcessInfoView(resourceBundle, dbBlocksJournalViewSash, SWT.NONE);
        dbBlocksJournalProcessInfoView.hideToolBar();
        dbBlocksJournalProcessInfoView.hide();

        blocksJournalTabItem.setControl(dbBlocksJournalViewSash);
    }

    private void createToolItems() {
        addDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        addDatabaseToolItem.setImage(ImageUtils.getImage(Images.ADD_DATABASE));
        addDatabaseToolItem.setToolTipText(l10n("add_db"));
        addDatabaseToolItem.addListener(SWT.Selection, event -> openAddNewDatabaseDialog());

        deleteDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        deleteDatabaseToolItem.setImage(ImageUtils.getImage(Images.DELETE_DATABASE));
        deleteDatabaseToolItem.setToolTipText(l10n("delete_db"));
        deleteDatabaseToolItem.setEnabled(false);
        deleteDatabaseToolItem.addListener(SWT.Selection, event -> deleteSelectedDatabase());

        editDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        editDatabaseToolItem.setImage(ImageUtils.getImage(Images.EDIT_DATABASE));
        editDatabaseToolItem.setToolTipText(l10n("edit_db"));
        editDatabaseToolItem.setEnabled(false);
        editDatabaseToolItem.addListener(SWT.Selection, event -> openEditSelectedDatabaseDialog());

        connectDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        connectDatabaseToolItem.setImage(ImageUtils.getImage(Images.CONNECT_DATABASE));
        connectDatabaseToolItem.setToolTipText(l10n("connect"));
        connectDatabaseToolItem.setEnabled(false);
        connectDatabaseToolItem.addListener(SWT.Selection, event -> connectToSelectedDatabase());

        disconnectDatabaseToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        disconnectDatabaseToolItem.setImage(ImageUtils.getImage(Images.DISCONNECT_DATABASE));
        disconnectDatabaseToolItem.setToolTipText(l10n("disconnect"));
        disconnectDatabaseToolItem.setEnabled(false);
        disconnectDatabaseToolItem.addListener(SWT.Selection, event -> disconnectSelectedDatabase());

        new ToolItem(view.getToolBar(), SWT.SEPARATOR);

        ToolItem showSettingsDialogToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        showSettingsDialogToolItem.setImage(ImageUtils.getImage(Images.SETTINGS));
        showSettingsDialogToolItem.setToolTipText(l10n("settings"));
        showSettingsDialogToolItem.addListener(SWT.Selection, event -> showSettingsDialog());

        ToolItem toggleLogsPanelVisibilityToolItem = new ToolItem(view.getToolBar(), SWT.CHECK);
        toggleLogsPanelVisibilityToolItem.setImage(ImageUtils.getImage(Images.SHOW_LOG_PANEL));
        toggleLogsPanelVisibilityToolItem.setToolTipText(l10n("show_logs_panel"));
        toggleLogsPanelVisibilityToolItem.setSelection(true);
        toggleLogsPanelVisibilityToolItem.addListener(SWT.Selection, event -> toggleLogsPanelVisibility(toggleLogsPanelVisibilityToolItem));

        new ToolItem(view.getToolBar(), SWT.SEPARATOR);

        autoUpdateToolItem = new ToolItem(view.getToolBar(), SWT.CHECK);
        autoUpdateToolItem.setImage(ImageUtils.getImage(Images.AUTOUPDATE));
        autoUpdateToolItem.setToolTipText(l10n("autoupdate"));
        autoUpdateToolItem.addListener(SWT.Selection, event -> setAutoUpdate(autoUpdateToolItem.getSelection()));
        autoUpdateToolItem.setSelection(settings.isAutoUpdate());

        showOnlyBlockedProcessesToolItem = new ToolItem(view.getToolBar(), SWT.CHECK);
        showOnlyBlockedProcessesToolItem.setImage(ImageUtils.getImage(Images.VIEW_ONLY_BLOCKED));
        showOnlyBlockedProcessesToolItem.setToolTipText(l10n("view_only_blocked"));
        showOnlyBlockedProcessesToolItem.addListener(SWT.Selection, event -> {
            if (showOnlyBlockedProcessesToolItem.getSelection()) {
                onlyBlockedFilter = new OnlyBlockedFilter();
                dbProcessView.getTreeViewer().addFilter(onlyBlockedFilter);
            } else {
                if (onlyBlockedFilter != null) {
                    dbProcessView.getTreeViewer().removeFilter(onlyBlockedFilter);
                }
            }
        });

        new ToolItem(view.getToolBar(), SWT.SEPARATOR);

        ToolItem openBlocksJournalToolItem = new ToolItem(view.getToolBar(), SWT.PUSH);
        openBlocksJournalToolItem.setImage(ImageUtils.getImage(Images.BLOCKS_JOURNAL_FOLDER));
        openBlocksJournalToolItem.setToolTipText(l10n("show_saved_blocks_journals"));
        openBlocksJournalToolItem.addListener(SWT.Selection, event -> {
            BlocksJournalView blocksJournalView = new BlocksJournalView(settings);
            blocksJournalView.open();
        });

    }

    private void changeToolItemsStateForController(DBController controller) {
        Display.getDefault().syncExec(() -> {
            boolean isDisconnected = controller != null && !controller.isConnected();
            connectDatabaseToolItem.setEnabled(isDisconnected);
            disconnectDatabaseToolItem.setEnabled(!isDisconnected);
            deleteDatabaseToolItem.setEnabled(isDisconnected);
            editDatabaseToolItem.setEnabled(isDisconnected);
        });
    }

    private void toggleLogsPanelVisibility(ToolItem toolItem) {
        boolean isShow = toolItem.getSelection();
        if (isShow) {
            toolItem.setImage(ImageUtils.getImage(Images.SHOW_LOG_PANEL));
            toolItem.setToolTipText(l10n("show_logs_panel"));
            PgSqlBlocks.getInstance().getApplicationController().getApplicationView().showBottomPanel();
        } else {
            toolItem.setImage(ImageUtils.getImage(Images.HIDE_LOG_PANEL));
            toolItem.setToolTipText(l10n("hide_logs_panel"));
            PgSqlBlocks.getInstance().getApplicationController().getApplicationView().hideBottomPanel();
        }
    }

    private void loadDatabases() {
        List<DBModel> dbModels = store.readObjects();
        List<DBModel> modelsWithDefault = dbModels.stream()
                .filter(m -> m.getVersion() == SupportedVersion.VERSION_DEFAULT)
                .collect(Collectors.toList());
        List<String> connectionNames = modelsWithDefault.stream().map(DBModel::getName).collect(Collectors.toList());
        if (!modelsWithDefault.isEmpty() && openUpdateDialog(connectionNames)) {
            updateVersions(modelsWithDefault);
            store.writeObjects(dbModels);
        }

        dbModels.forEach(this::addDatabase);
        dbModelsView.getTableViewer().refresh();
    }

    private void updateVersions(List<DBModel> dbModelsWithDefault) {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
        try {
            dialog.run(true, true, progressMonitor -> {
                progressMonitor.beginTask(l10n("update_version_dialog"), dbModelsWithDefault.size());
                for (DBModel dbModel : dbModelsWithDefault) {
                    if (progressMonitor.isCanceled()) {
                        LOG.info(l10n("update_version_cancelled_message"));
                        progressMonitor.done();
                        break;
                    } else {
                        DBController dbController = new DBController(settings, dbModel, this);
                        dbController.getVersion().ifPresent(v -> {
                            LOG.info("Обновлена версия сервера для подключения \"" + dbModel.getName()
                                        + "\". Новая версия: " + v.getVersion());
                            dbModel.setVersion(v);
                        });
                        progressMonitor.worked(1);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            LOG.error(l10n("update_version_error_message", e.getMessage()), e);
        }
    }

    private DBController addDatabase(DBModel dbModel) {
        return addDatabase(dbModel, dbControllers.size());
    }

    private DBController addDatabase(DBModel dbModel, int index) {
        DBController controller = new DBController(settings, dbModel, this);
        controller.addListener(this);
        dbControllers.add(index, controller);
        return controller;
    }

    private void deleteDatabase(DBController dbController) {
        dbController.removeListener(this);
        dbController.shutdown();
        dbControllers.remove(dbController);
        changeToolItemsStateForController(null);
        dbProcessView.getTreeViewer().setInput(null);
        dbBlocksJournalView.getTreeViewer().setInput(null);
    }

    private void saveDatabases() {
        List<DBModel> models = dbControllers.stream().map(DBController::getModel).collect(Collectors.toList());
        store.writeObjects(models);
    }

    private boolean openUpdateDialog(List<String> connectionNames) {
        MessageDialog dialog = new MessageDialog(view.getShell(),
                resourceBundle.getString("warning_title"),
                null,
                l10n("warning_text", connectionNames.stream().collect(Collectors.joining("\n*", "*", ""))),
                MessageDialog.WARNING, new String[]{"Ok", "Cancel"}, 0);
        return dialog.open() == 0;
    }

    private void openAddNewDatabaseDialog() {
        List<String> reservedConnectionNames = dbControllers.stream()
                .map(DBController::getModel)
                .map(DBModel::getName)
                .collect(Collectors.toList());
        AddDatabaseDialog addDatabaseDialog = new AddDatabaseDialog(resourceBundle, view.getShell(), reservedConnectionNames);
        if (addDatabaseDialog.open() == Window.OK) {
            DBController controller = addDatabase(addDatabaseDialog.getCreatedModel());
            dbModelsView.getTableViewer().refresh();
            if (controller.isEnabledAutoConnection()) {
                controller.connectAsync();
            }
            saveDatabases();
        }
    }

    private void editDatabase(DBModel oldModel, DBModel newModel) {
        Optional<DBController> opt = dbControllers.stream().filter(dbc -> dbc.getModel().equals(oldModel)).findFirst();
        opt.ifPresent(controller -> {
            controller.setModel(newModel);
            dbModelsView.getTableViewer().refresh(true, true);
            saveDatabases();
            if (controller.isEnabledAutoConnection()) {
                controller.connectAsync();
            }
        });
    }

    private void openEditSelectedDatabaseDialog() {
        if (dbModelsView.getTableViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
        List<String> reservedConnectionNames = dbControllers.stream()
                .map(DBController::getModel)
                .map(DBModel::getName)
                .collect(Collectors.toList());
        EditDatabaseDialog editDatabaseDialog =
                new EditDatabaseDialog(resourceBundle, view.getShell(), reservedConnectionNames, selectedController.getModel());
        if (editDatabaseDialog.open() == Window.OK) {
            editDatabase(editDatabaseDialog.getEditedModel(), editDatabaseDialog.getCreatedModel());
        }
    }

    private void deleteSelectedDatabase() {
        if (dbModelsView.getTableViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
        deleteDatabase(selectedController);
        dbModelsView.getTableViewer().refresh();
        saveDatabases();
    }

    private void connectToSelectedDatabase() {
        if (dbModelsView.getTableViewer().getStructuredSelection().getFirstElement() != null) {
            DBController selectedController = (DBController) dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
            selectedController.connectAsync();
        }
    }

    private void disconnectSelectedDatabase() {
        if (dbModelsView.getTableViewer().getStructuredSelection().getFirstElement() != null) {
            DBController selectedController = (DBController) dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
            selectedController.disconnect(true);
        }
    }

    private void updateProcessesInSelectedDatabase() {
        if (dbModelsView.getTableViewer().getStructuredSelection().getFirstElement() != null) {
            DBController selectedController = (DBController) dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
            selectedController.updateProcesses();
        }
    }

    private void setAutoUpdate(boolean autoUpdate) {
        settings.setAutoUpdate(autoUpdate);
    }

    private void showDBBlockedMessageInTray(DBController controller) {
        view.getDisplay().asyncExec(() -> {
            if (PgSqlBlocks.getInstance().getApplicationController().getApplicationView().trayIsAvailable()) {
                Tray tray = PgSqlBlocks.getInstance().getApplicationController().getApplicationView().getTray();
                TrayItem trayItem = tray.getItem(0);
                trayItem.setImage(ImageUtils.getImage(Images.BLOCKED));
                ToolTip toolTip = trayItem.getToolTip();
                toolTip.setMessage(l10n("db_has_lock", controller.getModel().getName()));
                toolTip.setVisible(true);
            } else {
                LOG.error("Tray is not available");
            }
        });
    }

    private void hideTrayMessageIfAllDatabasesUnblocked() {
        if (PgSqlBlocks.getInstance().getApplicationController().getApplicationView().trayIsAvailable()
                && dbControllers.stream().noneMatch(DBController::isBlocked)) {
            view.getDisplay().asyncExec(() -> {
                Tray tray = PgSqlBlocks.getInstance().getApplicationController().getApplicationView().getTray();
                TrayItem trayItem = tray.getItem(0);
                trayItem.setImage(ImageUtils.getImage(Images.UNBLOCKED));
                ToolTip toolTip = trayItem.getToolTip();
                toolTip.setVisible(false);
            });
        }
    }

    private void showSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog(settings, view.getShell());
        settingsDialog.open();
    }

    public void close() {
        dbControllers.forEach(DBController::shutdown);
        settings.removeListener(this);
    }

    @Override
    public void dbControllerStatusChanged(DBController controller, DBStatus newStatus) {
        view.getDisplay().asyncExec(() -> dbModelsView.getTableViewer().refresh(controller, true, true));
    }

    @Override
    public void dbControllerDidConnect(DBController controller) {
        if (settings.isAutoUpdate()) {
            controller.startProcessesUpdater();
        } else {
            controller.updateProcesses();
        }
        changeToolItemsStateForController(controller);
    }

    @Override
    public void dbControllerWillConnect(DBController controller) {
        LOG.info(l10n("db_connecting", controller.getModel().getName()));
    }

    @Override
    public void dbControllerConnectionFailed(DBController controller, SQLException exception) {
        LOG.error(controller.getModel().getName() + " " + exception.getMessage(), exception);
    }

    @Override
    public void dbControllerDisconnectFailed(DBController controller, boolean forcedByUser, SQLException exception) {
        if (!forcedByUser && settings.isAutoUpdate()) {
            LOG.info(l10n("db_disconnected_will_reconnect",
                    controller.getModel().getName(), settings.getUpdatePeriodSeconds()));
            controller.startProcessesUpdater(settings.getUpdatePeriodSeconds());
        } else {
            LOG.error(controller.getModel().getName() + " " + exception.getMessage(), exception);
        }
        changeToolItemsStateForController(controller);
    }

    @Override
    public void dbControllerDidDisconnect(DBController controller, boolean forcedByUser) {
        if (!forcedByUser && settings.isAutoUpdate()) {
            LOG.info(l10n("db_disconnected_will_reconnect",
                    controller.getModel().getName(), settings.getUpdatePeriodSeconds()));
            controller.startProcessesUpdater(settings.getUpdatePeriodSeconds());
        } else {
            LOG.info(l10n("db_disconnected", controller.getModel().getName()));
        }
        changeToolItemsStateForController(controller);
        controller.clear();
        dbProcessView.getTreeViewer().refresh();
        dbBlocksJournalView.getTreeViewer().refresh();
    }

    @Override
    public void dbControllerWillUpdateProcesses(DBController controller) {
        LOG.info(l10n("db_updating", controller.getModel().getName()));
    }

    @Override
    public void dbControllerProcessesUpdated(DBController controller) {
        view.getDisplay().asyncExec(() -> {
            dbModelsView.getTableViewer().refresh(controller);
            if (dbModelsView.getTableViewer().getStructuredSelection().getFirstElement() != null) {
                DBController selectedController = (DBController) dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
                if (controller.equals(selectedController)) {
                    dbProcessView.getTreeViewer().refresh();
                }
            }
        });
    }

    @Override
    public void dbControllerBlockedChanged(DBController controller) {
        if (settings.getShowToolTip()) {
            view.getDisplay().asyncExec(() -> dbModelsView.getTableViewer().refresh(controller));
            if (controller.isBlocked()) {
                showDBBlockedMessageInTray(controller);
            } else {
                hideTrayMessageIfAllDatabasesUnblocked();
            }
        }
    }

    @Override
    public void dbControllerBlocksJournalChanged(DBController controller) {
        view.getDisplay().asyncExec(() -> {
            if (dbModelsView.getTableViewer().getStructuredSelection().getFirstElement() != null) {
                DBController selectedController = (DBController) dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
                if (controller.equals(selectedController)) {
                    dbBlocksJournalView.getTreeViewer().refresh();
                }
            }
        });
    }

    @Override
    public String getPasswordFromUser(DBController controller) throws UserCancelException {
        final String[] pass = new String[1];

        Display.getDefault().syncExec(() -> {
            PasswordDialog passwordDialog = new PasswordDialog(resourceBundle, view.getShell(), controller.getModel());
            if (passwordDialog.open() == Window.OK) {
                pass[0] = passwordDialog.getPassword();
            } else {
                pass[0] = null;
            }
        });

        if (pass[0] != null) {
            return pass[0];
        } else {
            throw new UserCancelException();
        }
    }

    @Override
    public void dbModelsViewDidSelectController(DBController controller) {
        dbProcessView.getTreeViewer().setInput(controller.getProcesses());
        dbBlocksJournalView.getTreeViewer().setInput(controller.getBlocksJournal().getProcesses());
        changeToolItemsStateForController(controller);
    }

    @Override
    public void dbModelsViewDidCallActionToController(DBController controller) {
        if (!controller.isConnected()) {
            controller.connectAsync();
        } else {
            controller.disconnect(true);
        }
    }

    @Override
    public void dbModelsViewDidShowMenu(IMenuManager menuManager) {
        if (dbModelsView.getTableViewer().getStructuredSelection().getFirstElement() == null) {
            return;
        }
        DBController selectedController = (DBController) dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
        if (selectedController.isConnected()) {
            Action disconnectAction = new Action(l10n("disconnect"),
                    ImageDescriptor.createFromImage(ImageUtils.getImage(Images.DISCONNECT_DATABASE))) {
                @Override
                public void run() {
                    disconnectSelectedDatabase();
                }
            };
            menuManager.add(disconnectAction);
        } else {
            Action connectAction = new Action(l10n("connect"),
                    ImageDescriptor.createFromImage(ImageUtils.getImage(Images.CONNECT_DATABASE))) {
                @Override
                public void run() {
                    connectToSelectedDatabase();
                }
            };
            menuManager.add(connectAction);
            Action editAction = new Action(l10n("edit_db"),
                    ImageDescriptor.createFromImage(ImageUtils.getImage(Images.EDIT_DATABASE))) {
                @Override
                public void run() {
                    openEditSelectedDatabaseDialog();
                }
            };
            menuManager.add(editAction);
        }
        Action updateProcessesAction = new Action(l10n("update"),
                ImageDescriptor.createFromImage(ImageUtils.getImage(Images.UPDATE))) {
            @Override
            public void run() {
                updateProcessesInSelectedDatabase();
            }
        };
        menuManager.add(updateProcessesAction);
    }

    @Override
    public void settingsUpdatePeriodChanged(int updatePeriod) {
        if (settings.isAutoUpdate()) {
            dbControllers.stream().filter(DBController::isConnected).forEach(DBController::startProcessesUpdater);
        }
    }

    @Override
    public void settingsShowIdleChanged(boolean isShowIdle) {
        if (settings.isAutoUpdate()) {
            dbControllers.stream().filter(DBController::isConnected).forEach(DBController::startProcessesUpdater);
        }
    }

    @Override
    public void settingsShowBackendPidChanged(boolean isShowBackendPid) {
        if (settings.isAutoUpdate()) {
            dbControllers.stream().filter(DBController::isConnected).forEach(DBController::startProcessesUpdater);
        }
    }

    @Override
    public void settingsAutoUpdateChanged(boolean isAutoUpdate) {
        if (isAutoUpdate) {
            dbControllers.stream().filter(DBController::isConnected).forEach(DBController::startProcessesUpdater);
        } else {
            dbControllers.stream().filter(DBController::isConnected).forEach(DBController::stopProcessesUpdater);
        }
    }

    private void dbProcessesViewSelectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection.isEmpty()) {
            dbProcessInfoView.hide();
        } else {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            selectedProcesses = structuredSelection.toList();
            dbProcessInfoView.show(selectedProcesses.get(0));
        }
    }

    private void dbBlocksJournalViewSelectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection.isEmpty()) {
            dbBlocksJournalProcessInfoView.hide();
        } else {
            DBProcess process;
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object element = structuredSelection.getFirstElement();
            if (element instanceof DBBlocksJournalProcess) {
                DBBlocksJournalProcess blocksJournalProcess = (DBBlocksJournalProcess) element;
                process = blocksJournalProcess.getProcess();
            } else {
                process = (DBProcess) element;
            }
            dbBlocksJournalProcessInfoView.show(process);
        }
    }

    @Override
    public void dbProcessInfoViewTerminateProcessButtonClicked() {
        Object selectedController = dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
        if (selectedController == null || selectedProcesses.isEmpty()) {
            return;
        }
        List<Integer> pidProcessesList = selectedProcesses.stream().map(DBProcess::getPid).collect(Collectors.toList());
        terminateProcessesByPid((DBController) selectedController, pidProcessesList);
    }

    private void terminateProcessesByPid(DBController selectedController, List<Integer> pidProcessesList) {
        if (settings.isConfirmRequired() && !MessageDialog.openQuestion(view.getShell(), l10n("confirm_action"),
                l10n("kill_process_confirm_message", pidProcessesList))) {
            return;
        }

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
        try {
            dialog.run(true, true, progressMonitor -> {
                progressMonitor.beginTask("Termination processes", pidProcessesList.size());
                for (Integer processPid : pidProcessesList) {
                    if (progressMonitor.isCanceled()) {
                        LOG.info(l10n("kill_process_cancelled_message"));
                        progressMonitor.done();
                        break;
                    } else {
                        tryTerminateProcess(selectedController, processPid);
                        progressMonitor.worked(1);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            LOG.error(l10n("kill_process_error_message", e.getMessage()), e);
        } finally {
            selectedController.updateProcesses();
        }
    }

    private void tryTerminateProcess(DBController dbController, Integer pid) {
        try {
            boolean result = dbController.terminateProcessWithPid(pid);
            if (result) {
                LOG.info(l10n("process_terminated", dbController.getModel().getName(), pid));
            } else {
                LOG.info(l10n("process_not_terminated", dbController.getModel().getName(), pid, ""));
            }
        } catch (SQLException e) {
            LOG.error(l10n("process_not_terminated", dbController.getModel().getName(), pid, e.getMessage()), e);
        }
    }

    @Override
    public void dbProcessInfoViewCancelProcessButtonClicked() {
        Object selectedController = dbModelsView.getTableViewer().getStructuredSelection().getFirstElement();
        if (selectedController == null || selectedProcesses.isEmpty()) {
            return;
        }
        List<Integer> pidProcessesList = selectedProcesses.stream().map(DBProcess::getPid).collect(Collectors.toList());
        cancelProcessesByPid((DBController) selectedController, pidProcessesList);
    }

    private void cancelProcessesByPid(DBController selectedController, List<Integer> pidProcessesList) {
        if (settings.isConfirmRequired() && !MessageDialog.openQuestion(view.getShell(), l10n("confirm_action"),
                l10n("cancel_process_confirm_message", pidProcessesList))) {
            return;
        }

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
        try {
            dialog.run(true, true, progressMonitor -> {
                progressMonitor.beginTask("Cancel processes", pidProcessesList.size());
                for (Integer processPid : pidProcessesList) {
                    if (progressMonitor.isCanceled()) {
                        LOG.info(l10n("cancel_process_cancelled_message"));
                        progressMonitor.done();
                        break;
                    } else {
                        tryCancelProcess(selectedController, processPid);
                        progressMonitor.worked(1);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            LOG.error(l10n("cancel_process_error_message", e.getMessage()), e);
        } finally {
            selectedController.updateProcesses();
        }
    }

    private void tryCancelProcess(DBController dbController, Integer pid) {
        try {
            boolean result = dbController.cancelProcessWithPid(pid);
            if (result) {
                LOG.info(l10n("process_cancelled", dbController.getModel().getName(), pid));
            } else {
                LOG.info(l10n("process_not_cancelled", dbController.getModel().getName(), pid, ""));
            }
        } catch (SQLException e) {
            LOG.error(l10n("process_not_cancelled", dbController.getModel().getName(), pid, e.getMessage()), e);
        }
    }

    private String l10n(String msgId, Object... objects) {
        return String.format(resourceBundle.getString(msgId), objects);
    }

    class OnlyBlockedFilter extends ViewerFilter{

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (parentElement instanceof ArrayList) {
                DBProcess process = (DBProcess) element;
                return process.hasChildren();
            }
            return true;
        }
    }
}
