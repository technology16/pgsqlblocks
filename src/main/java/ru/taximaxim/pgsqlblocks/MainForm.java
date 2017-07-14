/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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
package ru.taximaxim.pgsqlblocks;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.blocksjournal.BlocksJournalContentProvider;
import ru.taximaxim.pgsqlblocks.blocksjournal.BlocksJournalLabelProvider;
import ru.taximaxim.pgsqlblocks.dbcdata.*;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeContentProvider;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeLabelProvider;
import ru.taximaxim.pgsqlblocks.ui.*;
import ru.taximaxim.pgsqlblocks.utils.*;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class MainForm extends ApplicationWindow implements IUpdateListener {

    private static final Logger LOG = Logger.getLogger(MainForm.class);
    
    private static final String APP_NAME = "pgSqlBlocks";
    private static final String SORT_DIRECTION = "sortDirection";
    private static final String SORT_COLUMN = "sortColumn";

    private static final int ZERO_MARGIN = 0;

    private static final int[] VERTICAL_WEIGHTS = new int[] {80, 20};
    private static final int[] HORIZONTAL_WEIGHTS = new int[] {12, 88};
    private static final int SASH_WIDTH = 2;
    // some problem with 512px: (SWT:4175): Gdk-WARNING **: gdk_window_set_icon_list: icons too large
    private static final int[] ICON_SIZES = { 32, 48, 256/*, 512*/ };
    
    private static final int TRAY_NOTIFICATION_MAX_LENGTH = 4;
    private static Display display;
    private Tray tray;

    private TreeViewer blocksJournalTreeViewer;

    private volatile DbcData selectedDbcData;
    private Process selectedProcess;
    private Text procText;
    private SashForm caTreeSf;
    private TableViewer caServersTable;
    private TreeViewer caMainTree;
    private Composite procComposite;
    private Action addDb;
    private Action deleteDB;
    private Action editDB;
    private Action connectDB;
    private Action disconnectDB;
    private Action update;
    private Action autoUpdate;
    private Action cancelUpdate;
    private Action logDisplay;
    private Action onlyBlocked;
    private ToolItem cancelProc;
    private ToolItem terminateProc;
    private TrayItem trayItem;
    private ToolTip tip;
    private static SortColumn sortColumn = SortColumn.BLOCKED_COUNT;
    private static SortDirection sortDirection = SortDirection.UP;
    private final Settings settings = Settings.getInstance();
    private FilterProcess filterProcess = FilterProcess.getInstance();
    private final DbcDataListBuilder dbcDataBuilder = DbcDataListBuilder.getInstance(this);
    private ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<>();
    private MenuManager serversTableMenuMgr = new MenuManager();
    private Set<SortColumn> visibleColumns = settings.getColumnsList();
    private boolean hasBlocks = false;
    private final ResourceBundle resourceBundle = settings.getResourceBundle();

    private Composite logComposite;
    private SashForm verticalSf;

    public static void main(String[] args) {
        try {
            MainForm mainForm = new MainForm();
            mainForm.setBlockOnOpen(true);
            display = Display.getCurrent();
            mainForm.open();
            display.dispose();
        } catch (Exception e) {
            LOG.error("An error has occurred:"+ e);
        }
    }

    public MainForm() {
        super(null);
        addToolBar(SWT.RIGHT | SWT.FLAT);
    }

    // TODO temporary getter, should not be used outside this class
    public static SortColumn getSortColumn() {
        return sortColumn;
    }

    // TODO temporary getter, should not be used outside this class
    public static SortDirection getSortDirection() {
        return sortDirection;
    }

    @Override
    protected void constrainShellSize() {
        super.constrainShellSize();
        getShell().setMaximized( true );
    }
    
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(APP_NAME);
        shell.setImages(loadIcons());
    }

    private Image[] loadIcons() {
        Image[] icons = new Image[ICON_SIZES.length];
        for (int i = 0; i < ICON_SIZES.length; ++i) {
            icons[i] = new Image(null,
                    getClass().getClassLoader().getResourceAsStream(MessageFormat.format("images/block-{0}x{0}.png",
                            ICON_SIZES[i])));
        }
        return icons;
    }

    @Override
    protected boolean canHandleShellCloseEvent() {
        if (settings.isConfirmExit() && !MessageDialog.openQuestion(getShell(), resourceBundle.getString("confirm_action"),
                resourceBundle.getString("exit_confirm_message"))) {
            return false;
        }
        dbcDataBuilder.getDbcDataList().forEach(DbcData::shutdown);
        return super.canHandleShellCloseEvent();
    }

    @Override
    protected Control createContents(Composite parent)
    {
        createApplicationMenu();

        createContent(parent);

        initTray();

        dbcDataBuilder.getDbcDataList().stream().filter(DbcData::isEnabledAutoConnect).forEach(DbcData::startUpdater);

        return parent;
    }

    private void createApplicationMenu() {
        Menu menuBar = new Menu(getShell(), SWT.BAR);
        MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        helpMenuHeader.setText("&pgSqlBlocks");

        Menu helpMenu = new Menu(getShell(), SWT.DROP_DOWN);
        helpMenuHeader.setMenu(helpMenu);

        MenuItem helpGetHelpItem = new MenuItem(helpMenu, SWT.PUSH);
        helpGetHelpItem.setText(resourceBundle.getString("about"));
        helpGetHelpItem.addListener(SWT.Selection, e -> new AboutDlg(getShell()).open());
        getShell().setMenuBar(menuBar);

        MenuItem exitMenuItem = new MenuItem(helpMenu, SWT.PUSH);
        exitMenuItem.setText(resourceBundle.getString("exit"));
        exitMenuItem.addListener(SWT.Selection, e -> getShell().close());
        getShell().setMenuBar(menuBar);
    }

    private void createContent(Composite composite) {
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);

        verticalSf = new SashForm(composite, SWT.VERTICAL);
        verticalSf.setLayout(layout);
        verticalSf.setLayoutData(gridData);
        verticalSf.SASH_WIDTH = SASH_WIDTH;
        verticalSf.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        createTopPanel(verticalSf);

        createLogPanel(verticalSf, layout);

        verticalSf.setWeights(VERTICAL_WEIGHTS);

        createStatusBar(composite, layout);
    }

    private void createLogPanel(SashForm sashForm, GridLayout layout) {
        logComposite = new Composite(sashForm, SWT.NONE);
        logComposite.setLayout(layout);
        logComposite.setVisible(settings.getShowLogMessages());

        UIAppender uiAppender = new UIAppender(logComposite, settings.getLocale());
        uiAppender.setThreshold(Level.INFO);
        Logger.getRootLogger().addAppender(uiAppender);
    }

    private void createStatusBar(Composite composite, GridLayout layout) {
        Composite statusBarComposite = new Composite(composite, SWT.NONE);
        statusBarComposite.setLayout(layout);
        statusBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        Label appVersionLabel = new Label(statusBarComposite, SWT.HORIZONTAL);
        appVersionLabel.setText("pgSqlBlocks v." + getAppVersion());
    }

    private void createTopPanel(SashForm sashForm) {
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout layout = new GridLayout();
        SashForm topPanelSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
        topPanelSashForm.setLayout(layout);
        topPanelSashForm.setLayoutData(layoutData);

        createDbcListPanel(topPanelSashForm);
        createTabPanel(topPanelSashForm);

        topPanelSashForm.setWeights(HORIZONTAL_WEIGHTS);

    }

    private void createDbcListPanel(SashForm sashForm) {
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        caServersTable = new TableViewer(sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        caServersTable.getTable().setHeaderVisible(true);
        caServersTable.getTable().setLayoutData(layoutData);
        TableViewerColumn tvColumn = new TableViewerColumn(caServersTable, SWT.NONE);
        tvColumn.getColumn().setText(resourceBundle.getString("database"));
        tvColumn.getColumn().setWidth(200);
        caServersTable.setContentProvider(new DbcDataListContentProvider());
        caServersTable.setLabelProvider(new DbcDataListLabelProvider());
        caServersTable.setInput(dbcDataBuilder.getDbcDataList());

        caServersTable.addSelectionChangedListener(event -> {
            if (!caServersTable.getSelection().isEmpty()) {
                IStructuredSelection selected = (IStructuredSelection) event.getSelection();
                DbcData newSelection = (DbcData) selected.getFirstElement();
                if (selectedDbcData != newSelection){
                    selectedDbcData = newSelection;
                    if(procComposite.isVisible()) {
                        procComposite.setVisible(false);
                        caTreeSf.layout(false, false);
                    }
                    serversToolBarState();
                    caMainTree.setInput(selectedDbcData.getProcess());
                    blocksJournalTreeViewer.setInput(selectedDbcData.getBlocksJournal().getProcesses());
                    updateUi();
                }
            }
        });

        caServersTable.addDoubleClickListener(event -> {
            if (!caServersTable.getSelection().isEmpty()) {
                IStructuredSelection selected = (IStructuredSelection) event.getSelection();
                selectedDbcData = (DbcData) selected.getFirstElement();
                if (selectedDbcData.getStatus() == DbcStatus.CONNECTED) {
                    dbcDataDisconnect();
                } else {
                    dbcDataConnect();
                }
            }
        });

        Menu mainMenu = serversTableMenuMgr.createContextMenu(caServersTable.getControl());
        serversTableMenuMgr.addMenuListener(manager -> {
            if (caServersTable.getSelection() instanceof IStructuredSelection) {
                manager.add(cancelUpdate);
                manager.add(update);
                manager.add(connectDB);
                manager.add(disconnectDB);
                manager.add(addDb);
                manager.add(editDB);
                manager.add(deleteDB);
            }
        });

        serversTableMenuMgr.setRemoveAllWhenShown(true);
        caServersTable.getControl().setMenu(mainMenu);
    }

    private void createTabPanel(SashForm sashForm) {
        GridLayout layout = new GridLayout();
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true,true);
        Composite topComposite = new Composite(sashForm, SWT.NONE);
        layout.marginHeight = ZERO_MARGIN;
        layout.marginWidth = ZERO_MARGIN;

        topComposite.setLayout(layout);
        topComposite.setLayoutData(layoutData);

        TabFolder tabPanel = new TabFolder(topComposite, SWT.BORDER);
        tabPanel.setLayoutData(layoutData);

        createTabs(tabPanel, layout, layoutData);
    }

    private void createTabs(TabFolder tabPanel, GridLayout gridLayout, GridData gridData) {
        createProcessesTab(tabPanel, gridLayout, gridData);
        createBlocksJournalTab(tabPanel, gridLayout, gridData);
    }

    private void createProcessesTab(TabFolder tabPanel, GridLayout gridLayout, GridData gridData) {

        TabItem processesTabItem = new TabItem(tabPanel, SWT.NONE);
        processesTabItem.setText(resourceBundle.getString("current_activity"));

        caTreeSf = new SashForm(tabPanel, SWT.VERTICAL);
        caMainTree = new TreeViewer(caTreeSf, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        caMainTree.getTree().setHeaderVisible(true);
        caMainTree.getTree().setLinesVisible(true);
        caMainTree.getTree().setLayoutData(gridData);
        fillTreeViewer(caMainTree);
        caMainTree.setContentProvider(new ProcessTreeContentProvider(settings));
        caMainTree.setLabelProvider(new ProcessTreeLabelProvider());
        ViewerFilter[] filters = new ViewerFilter[1];
        filters[0] = new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                return !(element instanceof Process) || filterProcess.isFiltered((Process) element);
            }
        };
        caMainTree.setFilters(filters);

        caMainTree.addSelectionChangedListener(event -> {
            if (!caMainTree.getSelection().isEmpty()) {
                IStructuredSelection selected = (IStructuredSelection) event.getSelection();
                selectedProcess = (Process) selected.getFirstElement();
                if(!procComposite.isVisible()) {
                    procComposite.setVisible(true);
                    caTreeSf.layout(true, true);
                }
                procText.setText(String.format("pid=%s%n%s", selectedProcess.getPid(), selectedProcess.getQuery()));
            }
        });

        procComposite = new Composite(caTreeSf, SWT.BORDER);
        procComposite.setLayout(gridLayout);
        GridData procCompositeGd = new GridData(SWT.FILL, SWT.FILL, true, true);
        procComposite.setLayoutData(procCompositeGd);
        procComposite.setVisible(false);
        ToolBar pcToolBar = new ToolBar(procComposite, SWT.FLAT | SWT.RIGHT);
        pcToolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        terminateProc = new ToolItem(pcToolBar, SWT.PUSH);
        terminateProc.setText(resourceBundle.getString("kill_process"));
        terminateProc.addListener(SWT.Selection, event -> {
            if (selectedProcess != null) {
                terminate(selectedProcess);
            }
        });

        cancelProc = new ToolItem(pcToolBar, SWT.PUSH);
        cancelProc.setText(resourceBundle.getString("cancel_process"));
        cancelProc.addListener(SWT.Selection, event -> {
            if (selectedProcess != null) {
                cancel(selectedProcess);
            }
        });

        procText = new Text(procComposite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        procText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        caTreeSf.setWeights(VERTICAL_WEIGHTS);

        processesTabItem.setControl(caTreeSf);
    }

    private void createBlocksJournalTab(TabFolder tabPanel, GridLayout gridLayout, GridData gridData) {
        TabItem tabItem = new TabItem(tabPanel, SWT.NONE);
        tabItem.setText(resourceBundle.getString("blocks_journal"));
        SashForm sashForm = new SashForm(tabPanel, SWT.HORIZONTAL);
        sashForm.setLayout(gridLayout);
        sashForm.setLayoutData(gridData);
        sashForm.SASH_WIDTH = SASH_WIDTH;

        blocksJournalTreeViewer = new TreeViewer(sashForm, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        blocksJournalTreeViewer.getTree().setHeaderVisible(true);
        blocksJournalTreeViewer.getTree().setLinesVisible(true);
        blocksJournalTreeViewer.getTree().setLayoutData(gridData);

        TreeViewerColumn pidColumn = new TreeViewerColumn(blocksJournalTreeViewer, SWT.NONE);
        pidColumn.getColumn().setText(SortColumn.PID.getName(resourceBundle));
        pidColumn.getColumn().setMoveable(true);
        pidColumn.getColumn().setToolTipText(SortColumn.PID.toString());
        pidColumn.getColumn().setWidth(SortColumn.PID.getColSize());

        TreeViewerColumn createDateColumn = new TreeViewerColumn(blocksJournalTreeViewer, SWT.NONE);
        createDateColumn.getColumn().setText(resourceBundle.getString("block_start_date"));
        createDateColumn.getColumn().setMoveable(true);
        createDateColumn.getColumn().setWidth(150);

        TreeViewerColumn closeDateColumn = new TreeViewerColumn(blocksJournalTreeViewer, SWT.NONE);
        closeDateColumn.getColumn().setText(resourceBundle.getString("block_change_date"));
        closeDateColumn.getColumn().setMoveable(true);
        closeDateColumn.getColumn().setWidth(150);

        List<SortColumn> columns = Arrays.stream(SortColumn.values())
                .filter(column -> column != SortColumn.PID)
                .collect(Collectors.toList());

        for (SortColumn column : columns) {
            TreeViewerColumn treeColumn = new TreeViewerColumn(blocksJournalTreeViewer, SWT.NONE);
            treeColumn.getColumn().setText(column.getName(resourceBundle));
            treeColumn.getColumn().setMoveable(true);
            treeColumn.getColumn().setToolTipText(column.toString());
            treeColumn.getColumn().setWidth(column.getColSize());
        }

        blocksJournalTreeViewer.setContentProvider(new BlocksJournalContentProvider());
        blocksJournalTreeViewer.setLabelProvider(new BlocksJournalLabelProvider());

        tabItem.setControl(sashForm);
    }

    private void initTray() {
        tray = display.getSystemTray();

        if (trayIsSupported()) {
            trayItem = new TrayItem(tray, SWT.NONE);
            trayItem.setImage(getIconImage());
            trayItem.setToolTipText("pgSqlBlocks v." + getAppVersion());
            final Menu trayMenu = new Menu(getShell(), SWT.POP_UP);
            MenuItem trayMenuItem = new MenuItem(trayMenu, SWT.PUSH);
            trayMenuItem.setText(resourceBundle.getString("exit"));
            trayMenuItem.addListener(SWT.Selection, event -> getShell().close());
            trayItem.addListener(SWT.MenuDetect, event -> trayMenu.setVisible(true));

            tip = new ToolTip(getShell(), SWT.BALLOON | SWT.ICON_WARNING);
            tip.setText("pgSqlBlocks v." + getAppVersion());
            tip.setAutoHide(true);
            tip.setVisible(false);
            trayItem.setToolTip(tip);
        } else {
            LOG.warn(resourceBundle.getString("system_tray_not_available_message"));
        }
    }

    private boolean trayIsSupported() {
        return tray != null;
    }

    private void fillTreeViewer(TreeViewer treeViewer) {
        for (SortColumn column : SortColumn.values()) {
            TreeViewerColumn treeColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
            treeColumn.getColumn().setText(column.getName(resourceBundle));
            treeColumn.getColumn().setData(SORT_COLUMN, column);
            treeColumn.getColumn().setData(SORT_DIRECTION, SortDirection.UP);
            treeColumn.getColumn().setMoveable(true);
            treeColumn.getColumn().setToolTipText(column.toString());

            boolean isVisible = visibleColumns.contains(column);
            treeColumn.getColumn().setWidth(isVisible ? column.getColSize() : 0);
            treeColumn.getColumn().setResizable(isVisible);

            TreeColumn swtColumn = treeColumn.getColumn();
            swtColumn.addListener(SWT.Selection, event -> {
                if (selectedDbcData != null) {
                    treeViewer.getTree().setSortColumn(swtColumn);
                    swtColumn.setData(SORT_DIRECTION, ((SortDirection)swtColumn.getData(SORT_DIRECTION)).getOpposite());
                    sortDirection = (SortDirection)swtColumn.getData(SORT_DIRECTION);
                    treeViewer.getTree().setSortDirection(sortDirection.getSwtData());
                    sortColumn = (SortColumn)swtColumn.getData(SORT_COLUMN);
                    selectedDbcData.getProcessTree(false);
                    updateUi();
                }
            });
        }
    }

    private void updateTreeViewer(TreeViewer treeViewer) {
        visibleColumns = settings.getColumnsList();
        TreeColumn[] treeColumns = treeViewer.getTree().getColumns();
        for (TreeColumn treeColumn : treeColumns) {
            SortColumn thisSortColumn = (SortColumn)treeColumn.getData(SORT_COLUMN);
            if (visibleColumns.contains(thisSortColumn)) {
                treeColumn.setWidth(thisSortColumn.getColSize());
                treeColumn.setResizable(true);
            } else {
                treeColumn.setWidth(0);
                treeColumn.setResizable(false);
            }
        }
    }

    private Image getIconImage() {
        if (dbcDataBuilder.getDbcDataList().stream().anyMatch(DbcData::hasBlockedProcess)) {
            return getImage(Images.BLOCKED);
        }
        return getImage(Images.UNBLOCKED);
    }

    @Override
    protected ToolBarManager createToolBarManager(int style) {
        ToolBarManager toolBarManager = new ToolBarManager(style);

        addDb = new Action(Images.ADD_DATABASE.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.ADD_DATABASE))) {
            
            @Override
            public void run() {
                AddDbcDataDlg addDbcDlg = new AddDbcDataDlg(getShell(), null,
                                                            dbcDataBuilder.getDbcDataList(), resourceBundle);
                if (Window.OK == addDbcDlg.open()) {
                    selectedDbcData = addDbcDlg.getNewDbcData();
                    if (selectedDbcData != null) {
                        dbcDataBuilder.add(selectedDbcData);
                        caServersTable.getTable().setSelection(dbcDataBuilder.getDbcDataList().indexOf(selectedDbcData));
                    }
                    serversToolBarState();
                    updateUi();
                }
            }
        };

        toolBarManager.add(addDb);

        deleteDB = new Action(Images.DELETE_DATABASE.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.DELETE_DATABASE))) {
            @Override
            public void run() {
                if (MessageDialog.openQuestion(getShell(),
                        resourceBundle.getString("confirm_action"),
                        MessageFormat.format(resourceBundle.getString("delete_confirm_message"), selectedDbcData.getName()))) {
                    dbcDataBuilder.delete(selectedDbcData);
                    selectedDbcData = null;
                    caMainTree.setInput(null);
                    blocksJournalTreeViewer.setInput(null);
                    updateUi();
                }
            }
        };

        deleteDB.setEnabled(false);
        toolBarManager.add(deleteDB);

        editDB = new Action(Images.EDIT_DATABASE.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.EDIT_DATABASE))) {

            @Override
            public void run() {
                AddDbcDataDlg editDbcDlg = new AddDbcDataDlg(getShell(), selectedDbcData,
                                                                dbcDataBuilder.getDbcDataList(), resourceBundle);
                if (Window.OK == editDbcDlg.open()) {
                    DbcData oldOne = editDbcDlg.getEditedDbcData();
                    DbcData newOne = editDbcDlg.getNewDbcData();

                    dbcDataBuilder.edit(oldOne, newOne);
                    updateUi();
                }
            }
        };

        editDB.setEnabled(false);
        toolBarManager.add(editDB);

        toolBarManager.add(new Separator());

        connectDB = new Action(Images.CONNECT_DATABASE.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.CONNECT_DATABASE))) {
            
            @Override
            public void run() {
                dbcDataConnect();
            }
        };

        connectDB.setEnabled(false);
        toolBarManager.add(connectDB);

        disconnectDB = new Action(Images.DISCONNECT_DATABASE.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.DISCONNECT_DATABASE))) {
            
            @Override
            public void run() {
                dbcDataDisconnect();
            }
        };

        disconnectDB.setEnabled(false);
        toolBarManager.add(disconnectDB);

        toolBarManager.add(new Separator());

        update =  new Action(Images.UPDATE.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.UPDATE))) {
            
            @Override
            public void run() {
                if (selectedDbcData != null) {
                    selectedDbcData.startUpdater();
                }
            }
        };

        toolBarManager.add(update);

        autoUpdate = new Action(Images.AUTOUPDATE.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.AUTOUPDATE))) {
            
            @Override
            public void run() {
                settings.setAutoUpdate(autoUpdate.isChecked());
                if (autoUpdate.isChecked()) {
                    dbcDataBuilder.getDbcDataList().stream()
                            .filter(x -> x.isConnected() || x.isInUpdateState())
                            .filter(x -> x.getStatus() != DbcStatus.CONNECTION_ERROR)
                            .forEach(DbcData::startUpdater);
                } else {
                    dbcDataBuilder.getDbcDataList().forEach(DbcData::stopUpdater);
                }
            }
        };

        autoUpdate.setChecked(settings.isAutoUpdate());
        toolBarManager.add(autoUpdate);

        cancelUpdate = new Action(Images.CANCEL_UPDATE.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.CANCEL_UPDATE))) {

            @Override
            public void run() {
                throw new RuntimeException("Not implemented");
/*                if (selectedDbcData != null && selectedDbcData.isConnected()) {
                    // TODO dbcDataBuilder.removeOnceScheduledUpdater(selectedDbcData.);
                    // TODO selectedDbcData.setStatus(DbcStatus.CONNECTED); - wny?
                }*/
            }
        };

        cancelUpdate.setEnabled(false);

        toolBarManager.add(new Separator());

        Action filterSetting = new Action(Images.FILTER.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.FILTER))) {

            @Override
            public void run() {
                FilterDlg filterDlg = new FilterDlg(getShell(), filterProcess);
                filterDlg.open();
                updateUi();
            }
        };
        
        toolBarManager.add(filterSetting);

        onlyBlocked = new Action(Images.VIEW_ONLY_BLOCKED.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.VIEW_ONLY_BLOCKED))) {
            
            @Override
            public void run() {
                settings.setOnlyBlocked(onlyBlocked.isChecked());
                updateUi();
            }
        };

        onlyBlocked.setChecked(settings.isOnlyBlocked());
        toolBarManager.add(onlyBlocked);

        toolBarManager.add(new Separator());

        toolBarManager.add(new Separator());

        Action settingsAction = new Action(Images.SETTINGS.getDescription(resourceBundle),
                ImageDescriptor.createFromImage(getImage(Images.SETTINGS))) {

            @Override
            public void run() {
                SettingsDlg settingsDlg = new SettingsDlg(getShell(), settings);
                if (Window.OK == settingsDlg.open()) {
                    updateUi();
                    dbcDataBuilder.getDbcDataList().forEach(DbcData::stopUpdater);

                    dbcDataBuilder.getDbcDataList().stream()
                            .filter(x -> x.isConnected() || x.isEnabledAutoConnect())
                            //.filter(x -> x.getStatus() != DbcStatus.CONNECTION_ERROR) // ok, update those too for now
                            .forEach(DbcData::startUpdater);
                    updateTreeViewer(caMainTree);
                }
            }
        };

        toolBarManager.add(settingsAction);

        logDisplay = new Action() {
            @Override
            public void run() {
                settings.setShowLogMessages(logDisplay.isChecked());
                logComposite.setVisible(logDisplay.isChecked());
                verticalSf.setWeights(VERTICAL_WEIGHTS);
                updateLogDisplayActionBtn(logDisplay.isChecked());
            }
        };

        logDisplay.setChecked(settings.getShowLogMessages());
        updateLogDisplayActionBtn(settings.getShowLogMessages());
        toolBarManager.add(logDisplay);

        return toolBarManager;
    }

    void updateLogDisplayActionBtn(boolean showLogMessages) {
        Images image = showLogMessages ? Images.SHOW_LOG_PANEL : Images.HIDE_LOG_PANEL;
        logDisplay.setText(image.getDescription(resourceBundle));
        logDisplay.setImageDescriptor(ImageDescriptor.createFromImage(getImage(image)));
    }

    private void checkBlocks() {
        if (trayIsSupported() && settings.getShowToolTip()) {
            List<String> blockedDB = dbcDataBuilder.getDbcDataList().stream()
                    .filter(DbcData::isConnected)
                    .filter(DbcData::hasBlockedProcess)
                    .limit(TRAY_NOTIFICATION_MAX_LENGTH)
                    .map(DbcData::getName)
                    .collect(Collectors.toList());

            boolean newHasBlocks = !blockedDB.isEmpty();

            if (newHasBlocks && !hasBlocks) {
                String message = MessageFormat.format("{0}: {1}",
                        blockedDB.size() == 1 ? resourceBundle.getString("one_db_has_lock") : resourceBundle.getString("several_db_has_lock"),
                        String.join(", \n", blockedDB));
                tip.setMessage(message);
                tip.setVisible(true);
            }
            hasBlocks = newHasBlocks;
        }
    }

    private Image getImage(Images type) {
        return imagesMap.computeIfAbsent(type.toString(),
                k -> new Image(null, getClass().getClassLoader().getResourceAsStream(type.getImageAddr())));
    }
    
    private String getAppVersion() {
        URL manifestPath = MainForm.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
        Manifest manifest = null;
        try {
            manifest = new Manifest(manifestPath != null ? manifestPath.openStream() : null);
        } catch (IOException e) {
            LOG.error(resourceBundle.getString("error_reading_the_manifest"), e);
        }
        Attributes manifestAttributes = manifest != null ? manifest.getMainAttributes() : null;
        String appVersion = manifestAttributes != null ? manifestAttributes.getValue("Implementation-Version") : null;
        if(appVersion == null) {
            return "";
        }
        return appVersion;
    }
    
    private void terminate(Process process) {
        int pid = process.getPid();
        if (settings.isConfirmRequired() && !MessageDialog.openQuestion(getShell(), resourceBundle.getString("confirm_action"),
                MessageFormat.format(resourceBundle.getString("kill_process_confirm_message"), pid))) {
            return;
        }
        String term = "select pg_terminate_backend(?);";
        boolean kill = false;
        try (PreparedStatement termPs = selectedDbcData.getConnection().prepareStatement(term)) {
            termPs.setInt(1, pid);
            try (ResultSet resultSet = termPs.executeQuery()) {
                if (resultSet.next()) {
                    kill = resultSet.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            LOG.error(selectedDbcData.getName() + " " + e.getMessage(), e);
        }
        if(kill) {
            LOG.info(MessageFormat.format(resourceBundle.getString("process_terminated"), selectedDbcData.getName(), pid));
            selectedDbcData.startUpdater();
        } else {
            LOG.info(MessageFormat.format(resourceBundle.getString("process_not_terminated"), selectedDbcData.getName(), pid));
        }
    }

    private void cancel(Process process) {
        int pid = process.getPid();
        if (settings.isConfirmRequired() && !MessageDialog.openQuestion(getShell(), resourceBundle.getString("confirm_action"),
            MessageFormat.format(resourceBundle.getString("cancel_process_confirm_message"), pid))) {
            return;
        }
        String cancel = "select pg_cancel_backend(?);";
        boolean kill = false;
        try (PreparedStatement cancelPs = selectedDbcData.getConnection().prepareStatement(cancel)) {
            cancelPs.setInt(1, pid);
            try (ResultSet resultSet = cancelPs.executeQuery()) {
                if (resultSet.next()) {
                    kill = resultSet.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            LOG.error(selectedDbcData.getName() + " " + e.getMessage(), e);
        }
        if(kill) {
            LOG.info(MessageFormat.format(resourceBundle.getString("process_cancelled"), selectedDbcData.getName(), pid));
            selectedDbcData.startUpdater();
        } else {
            LOG.info(MessageFormat.format(resourceBundle.getString("process_not_cancelled"), selectedDbcData.getName(), pid));
        }
    }

    private void dbcDataConnect() {
        synchronized (selectedDbcData) {
            caMainTree.setInput(selectedDbcData.getProcess());
            blocksJournalTreeViewer.setInput(selectedDbcData.getBlocksJournal().getProcesses());
            selectedDbcData.startUpdater();
            connectState();
        }
    }
    
    private void dbcDataDisconnect() {
        synchronized (selectedDbcData) {
            LOG.debug(MessageFormat.format(resourceBundle.getString("remove_dbcdata_on_disconnect"),
                    selectedDbcData.getName()));
            selectedDbcData.stopUpdater();
            selectedDbcData.disconnect();
            disconnectState();
        }
        updateUi();
    }
    
    private void serversToolBarState() {
        if (selectedDbcData != null &&
                (selectedDbcData.getStatus() == DbcStatus.CONNECTION_ERROR ||
                selectedDbcData.getStatus() == DbcStatus.DISABLED)) {
            
            disconnectState();
        } else {
            connectState();
        }
    }
    
    private void connectState() {
        deleteDB.setEnabled(false);
        editDB.setEnabled(false);
        connectDB.setEnabled(false);
        disconnectDB.setEnabled(true);
        cancelUpdate.setEnabled(true);
        cancelProc.setEnabled(true);
        terminateProc.setEnabled(true);
    }
    
    private void disconnectState() {
        deleteDB.setEnabled(true);
        editDB.setEnabled(true);
        connectDB.setEnabled(true);
        disconnectDB.setEnabled(false);
        cancelUpdate.setEnabled(false);
        cancelProc.setEnabled(false);
        terminateProc.setEnabled(false);
    }

    private void updateUi() {
        display.asyncExec(() -> {
            if (!display.isDisposed()) {
                caServersTable.refresh();
                serversToolBarState();
                caMainTree.refresh();
                blocksJournalTreeViewer.refresh();
            }
            trayItem.setImage(getIconImage());
            checkBlocks();
        });
    }

    @Override
    public void serverUpdated() {
        updateUi();
    }
}
