package ru.taximaxim.pgsqlblocks;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeColumn;

import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcDataListBuilder;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcDataListContentProvider;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcDataListLabelProvider;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcStatus;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeBuilder;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeContentProvider;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeLabelProvider;
import ru.taximaxim.pgsqlblocks.ui.AddDbcDataDlg;
import ru.taximaxim.pgsqlblocks.ui.SettingsDlg;
import ru.taximaxim.pgsqlblocks.ui.UIAppender;
import ru.taximaxim.pgsqlblocks.utils.Images;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.Settings;


public class MainForm extends ApplicationWindow {

    private static final Logger LOG = Logger.getLogger(MainForm.class);
    
    private static final String APP_NAME = "pgSqlBlocks";
    private static final String SORT_DIRECTION = "sortDirection";
    private static final String PID = " pid=";
    private static final int ZERO_MARGIN = 0;
    private static final int[] VERTICAL_WEIGHTS = new int[] {80, 20};
    private static final int[] HORIZONTAL_WEIGHTS = new int[] {12, 88};
    private static final int SASH_WIDTH = 2;
    
    private static Display display;
    private static Shell shell;
    
    private Process updateProcces;
    private DbcData selectedDbcData;
    private Process selectedProcess;
    private Text procText;
    private SashForm caTreeSf;
    private TableViewer caServersTable;
    private TreeViewer caMainTree;
    private Composite procComposite;
    private TableViewer bhServersTable;
    private TreeViewer bhMainTree;
    private Action deleteDB;
    private Action editDB;
    private Action connectDB;
    private Action disconnectDB;
    private Action autoUpdate;
    private Action onlyBlocked;
    private AddDbcDataDlg addDbcDlg;
    private boolean autoUpdateMode = true;
    private boolean onlyBlockedMode = false;
    private SortColumn sortColumn = SortColumn.BLOCKED_COUNT;
    private SortDirection sortDirection = SortDirection.UP;
    private Settings settings = Settings.getInstance();
    private DbcDataListBuilder dbcDataList = DbcDataListBuilder.getInstance();
    private ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<String, Image>();
    private ConcurrentMap<DbcData, ProcessTreeBuilder> processTreeMap = new ConcurrentHashMap<>();
    private ConcurrentMap<DbcData, ProcessTreeBuilder> blockedProcessTreeMap = new ConcurrentHashMap<>();
    
    private int[] caMainTreeColsSize = new int[]{80, 110, 150, 110, 110, 110, 145, 145, 145, 55, 145, 70, 65, 150, 80};
    private String[] caMainTreeColsName = new String[]{
            "pid", "blocked_count", "application_name", "datname", "usename", "client", "backend_start", "query_start",
            "xact_stat", "state", "state_change", "blocked", "waiting", "query" , "slowquery"};
    
    private String[] caColName = {"PID", "BLOCKED_COUNT", "APPLICATION_NAME", "DATNAME", "USENAME", "CLIENT", "BACKEND_START", "QUERY_START",
            "XACT_STAT", "STATE", "STATE_CHANGE", "BLOCKED", "WAITING", "QUERY", "SLOWQUERY"};
    
    public Process getProcessTree(DbcData dbcData) {
        ProcessTreeBuilder processTree = processTreeMap.get(dbcData);
        if(processTree == null){
            processTree = new ProcessTreeBuilder(dbcData);
            processTreeMap.put(dbcData, processTree);
        }
        Process rootProcess = processTree.getProcessTree();
        processTree.processSort(rootProcess, sortColumn, sortDirection);
        return rootProcess;
    }
    
    public Process getOnlyBlockedProcessTree(DbcData dbcData) {
        ProcessTreeBuilder processTree = blockedProcessTreeMap.get(dbcData);
        if(processTree == null){
            processTree = new ProcessTreeBuilder(dbcData);
            blockedProcessTreeMap.put(dbcData, processTree);
        }
        
        return processTree.getOnlyBlockedProcessTree();
    }
    
    public static void main(String[] args) {
        try {
            Logger rootLogger = Logger.getRootLogger();
            rootLogger.setLevel(Level.INFO);
            PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
            rootLogger.addAppender(new ConsoleAppender(layout));
            try {
                DailyRollingFileAppender fileAppender = new DailyRollingFileAppender(layout, PathBuilder.getInstance().getLogsPath().toString(), "yyyy-MM-dd");
                rootLogger.addAppender(fileAppender);
            } catch (IOException e) {
                LOG.error("Произошла ошибка при настройке логирования:", e);
            }
            display = new Display();
            shell = new Shell(display);
            MainForm wwin = new MainForm(shell);
            wwin.setBlockOnOpen(true);
            wwin.open();
            display.dispose();
        } catch (Exception e) {
            LOG.error("Произошла ошибка:", e);
        }
    }

    public MainForm(Shell shell) {
        super(shell);
        addToolBar(SWT.RIGHT | SWT.FLAT);
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
    }

    @Override
    protected boolean canHandleShellCloseEvent() {
        if (!MessageDialog.openQuestion(getShell(), "Подтверждение действия",
                "Вы действительно хотите выйти из pgSqlBlocks?")) {
            return false;
        }
        return super.canHandleShellCloseEvent();
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        
        addDbcDlg = new AddDbcDataDlg(shell);

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = ZERO_MARGIN;
        gridLayout.marginWidth = ZERO_MARGIN;
        
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        
        SashForm verticalSf = new SashForm(composite, SWT.VERTICAL);
        {
            verticalSf.setLayout(gridLayout);
            verticalSf.setLayoutData(gridData);
            verticalSf.SASH_WIDTH = SASH_WIDTH;
            verticalSf.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            
            Composite topComposite = new Composite(verticalSf, SWT.NONE);
            topComposite.setLayout(gridLayout);
            
            TabFolder tabPanel = new TabFolder(topComposite, SWT.BORDER);
            {
                tabPanel.setLayoutData(gridData);
                TabItem currentActivityTi = new TabItem(tabPanel, SWT.NONE);
                {
                    currentActivityTi.setText("Текущая активность");
                    SashForm currentActivitySf = new SashForm(tabPanel, SWT.HORIZONTAL);
                    {
                        currentActivitySf.setLayout(gridLayout);
                        currentActivitySf.setLayoutData(gridData);
                        currentActivitySf.SASH_WIDTH = SASH_WIDTH;
                        currentActivitySf.setBackground(topComposite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                        
                        caServersTable = new TableViewer(currentActivitySf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                        {
                            caServersTable.getTable().setHeaderVisible(true);
                            caServersTable.getTable().setLinesVisible(true);
                            caServersTable.getTable().setLayoutData(gridData);
                            TableViewerColumn tvColumn = new TableViewerColumn(caServersTable, SWT.NONE);
                            tvColumn.getColumn().setText("Сервер");
                            tvColumn.getColumn().setWidth(200);
                            caServersTable.setContentProvider(new DbcDataListContentProvider());
                            caServersTable.setLabelProvider(new DbcDataListLabelProvider());
                            caServersTable.setInput(dbcDataList.getList());
                            selectedDbcData = dbcDataList.getList().stream()
                                    .filter(dbcData -> dbcData.getStatus() == DbcStatus.CONNECTED).findFirst().orElse(null);
                            caServersTable.refresh();
                            updateTree();
                        }
                        
                        caTreeSf = new SashForm(currentActivitySf, SWT.VERTICAL);
                        {
                            caMainTree = new TreeViewer(caTreeSf, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                            caMainTree.getTree().setHeaderVisible(true);
                            caMainTree.getTree().setLinesVisible(true);
                            caMainTree.getTree().setLayoutData(gridData);
                            for(int i=0;i<caMainTreeColsName.length;i++) {
                                TreeViewerColumn treeColumn = new TreeViewerColumn(caMainTree, SWT.NONE);
                                treeColumn.getColumn().setText(caMainTreeColsName[i]);
                                treeColumn.getColumn().setWidth(caMainTreeColsSize[i]);
                                treeColumn.getColumn().setData("colName",caColName[i]);
                                treeColumn.getColumn().setData(SORT_DIRECTION, SortDirection.UP);
                            }
                            caMainTree.setContentProvider(new ProcessTreeContentProvider());
                            caMainTree.setLabelProvider(new ProcessTreeLabelProvider());
                            display.timerExec(1000, timer);
                            
                            procComposite = new Composite(caTreeSf, SWT.BORDER);
                            {
                                procComposite.setLayout(gridLayout);
                                GridData procCompositeGd = new GridData(SWT.FILL, SWT.FILL, true, true);
                                procComposite.setLayoutData(procCompositeGd);
                                procComposite.setVisible(false);
                                ToolBar pcToolBar = new ToolBar(procComposite, SWT.FLAT | SWT.RIGHT);
                                pcToolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
                                ToolItem terminateProc = new ToolItem(pcToolBar, SWT.PUSH);
                                terminateProc.setText("Уничтожить процесс");
                                terminateProc.addListener(SWT.Selection, event -> {
                                    if (selectedProcess != null) {
                                        terminate(selectedProcess);
                                        updateTree();
                                    }
                                });
                                
                                ToolItem cancelProc = new ToolItem(pcToolBar, SWT.PUSH);
                                cancelProc.setText("Послать сигнал отмены процесса");
                                cancelProc.addListener(SWT.Selection, event -> {
                                    if (selectedProcess != null) {
                                        cancel(selectedProcess);
                                        updateTree();
                                    }
                                });

                                procText = new Text(procComposite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
                                procText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                                procText.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
                            }
                        }
                        caTreeSf.setWeights(VERTICAL_WEIGHTS);
                    }
                    currentActivitySf.setWeights(HORIZONTAL_WEIGHTS);
                    currentActivityTi.setControl(currentActivitySf);
                }
                
                TabItem blocksHistoryTi = new TabItem(tabPanel, SWT.NONE);
                {
                    blocksHistoryTi.setText("История блокировок");
                    SashForm blocksHistorySf = new SashForm(tabPanel, SWT.HORIZONTAL);
                    {
                        blocksHistorySf.setLayout(gridLayout);
                        blocksHistorySf.setLayoutData(gridData);
                        blocksHistorySf.SASH_WIDTH = SASH_WIDTH;
                        blocksHistorySf.setBackground(topComposite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

                        bhServersTable = new TableViewer(blocksHistorySf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                        {
                            bhServersTable.getTable().setHeaderVisible(true);
                            bhServersTable.getTable().setLinesVisible(true);
                            bhServersTable.getTable().setLayoutData(gridData);
                            TableViewerColumn serversTc = new TableViewerColumn(bhServersTable, SWT.NONE);
                            serversTc.getColumn().setText("Сервер");
                            serversTc.getColumn().setWidth(200);
                            bhServersTable.setContentProvider(new DbcDataListContentProvider());
                            bhServersTable.setLabelProvider(new DbcDataListLabelProvider());
                        }

                        bhMainTree = new TreeViewer(blocksHistorySf, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                        {
                            bhMainTree.getTree().setHeaderVisible(true);
                            bhMainTree.getTree().setLinesVisible(true);
                            bhMainTree.getTree().setLayoutData(gridData);
                            for(int i = 0; i < caMainTreeColsName.length; i++) {
                                TreeViewerColumn treeColumn = new TreeViewerColumn(bhMainTree, SWT.NONE);
                                treeColumn.getColumn().setText(caMainTreeColsName[i]);
                                treeColumn.getColumn().setWidth(caMainTreeColsSize[i]);
                            }
                            bhMainTree.setContentProvider(new ProcessTreeContentProvider());
                            bhMainTree.setLabelProvider(new ProcessTreeLabelProvider());
                        }
                    }
                    blocksHistorySf.setWeights(HORIZONTAL_WEIGHTS);
                    blocksHistoryTi.setControl(blocksHistorySf);
                }
            }
            Composite logComposite = new Composite(verticalSf, SWT.NONE);
            {
                logComposite.setLayout(gridLayout);
            }
            verticalSf.setWeights(VERTICAL_WEIGHTS);
            UIAppender uiAppender = new UIAppender(logComposite);
            uiAppender.setThreshold(Level.INFO);
            Logger.getRootLogger().addAppender(uiAppender);
            
            Composite statusBar = new Composite(composite, SWT.NONE);
            {
                statusBar.setLayout(gridLayout);
                statusBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
                Label appVersionLabel = new Label(statusBar, SWT.HORIZONTAL);
                appVersionLabel.setText("PgSqlBlocks v." + getAppVersion());
            }
        }

        caMainTree.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (!caMainTree.getSelection().isEmpty()) {
                    IStructuredSelection selected = (IStructuredSelection) event.getSelection();
                    selectedProcess = (Process) selected.getFirstElement();
                    if(!procComposite.isVisible()) {
                        procComposite.setVisible(true);
                        caTreeSf.layout(true, true);
                    }
                    procText.setText(String.format("pid=%s%n%s", selectedProcess.getPid(), selectedProcess.getQuery()));
                }
            }
        });
        
        caServersTable.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (!caServersTable.getSelection().isEmpty()) {
                    IStructuredSelection selected = (IStructuredSelection) event.getSelection();
                    selectedDbcData = (DbcData) selected.getFirstElement();
                    if(procComposite.isVisible()) {
                        procComposite.setVisible(false);
                        caTreeSf.layout(false, false);
                    }
                    serversToolBarState();
                    updateTree();
                }
            }
        });
        
        for (TreeColumn column : caMainTree.getTree().getColumns()) {
            column.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    caMainTree.getTree().setSortColumn(column);
                    column.setData(SORT_DIRECTION, ((SortDirection)column.getData(SORT_DIRECTION)).getOpposite());
                    sortDirection = (SortDirection)column.getData(SORT_DIRECTION);
                    caMainTree.getTree().setSortDirection(sortDirection.getSwtData());
                    sortColumn = SortColumn.valueOf((String)column.getData("colName"));
                    updateTree();
                }
            });
        }
        
        caServersTable.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                if (!caServersTable.getSelection().isEmpty()) {
                    IStructuredSelection selected = (IStructuredSelection) event.getSelection();
                    selectedDbcData = (DbcData) selected.getFirstElement();
                    if (selectedDbcData.getStatus() == DbcStatus.CONNECTED) {
                        dbcDataDisconnect();
                    } else {
                        dbcDataConnect();
                    }
                }
            }
        });
        
        return parent;
    }
    
    protected ToolBarManager createToolBarManager(int style) {
        ToolBarManager toolBarManager = new ToolBarManager(style);

        Action addDb = new Action(Images.ADD_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.ADD_DATABASE))) {
            
            @Override
            public void run() {
                addDbcDlg.open();
                serversToolBarState();
                caServersTable.refresh();
                updateTree();
            }
        };

        toolBarManager.add(addDb);

        deleteDB = new Action(Images.DELETE_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.DELETE_DATABASE))) {
            
            @Override
            public void run() {
                boolean okPress = MessageDialog.openQuestion(shell,
                        "Подтверждение действия",
                        String.format("Вы действительно хотите удалить %s?", selectedDbcData.getName()));
                if (okPress) {
                    processTreeMap.remove(selectedDbcData);
                    blockedProcessTreeMap.remove(selectedDbcData);
                    dbcDataList.delete(selectedDbcData);
                    if (dbcDataList.getList().size() > 0) {
                        selectedDbcData = dbcDataList.getList().get(dbcDataList.getList().size() - 1);
                    } else {
                        selectedDbcData = null;
                    }
                }
                caServersTable.refresh();
                updateTree();
            }
        };

        deleteDB.setEnabled(false);
        toolBarManager.add(deleteDB);

        editDB = new Action(Images.EDIT_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.EDIT_DATABASE))) {
            
            @Override
            public void run() {
                AddDbcDataDlg editDbcDlg = new AddDbcDataDlg(shell, selectedDbcData);
                editDbcDlg.open();
                processTreeMap.remove(selectedDbcData);
                blockedProcessTreeMap.remove(selectedDbcData);
                selectedDbcData = dbcDataList.getList().get(dbcDataList.getList().size() - 1);
                serversToolBarState();
                caServersTable.refresh();
                updateTree();
            }
        };

        editDB.setEnabled(false);
        toolBarManager.add(editDB);

        toolBarManager.add(new Separator());

        connectDB = new Action(Images.CONNECT_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.CONNECT_DATABASE))) {
            
            @Override
            public void run() {
                dbcDataConnect();
            }
        };

        connectDB.setEnabled(false);
        toolBarManager.add(connectDB);

        disconnectDB = new Action(Images.DISCONNECT_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.DISCONNECT_DATABASE))) {
            
            @Override
            public void run() {
                dbcDataDisconnect();
            }
        };

        disconnectDB.setEnabled(false);
        toolBarManager.add(disconnectDB);

        toolBarManager.add(new Separator());

        Action update = new Action(Images.UPDATE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.UPDATE))) {
            
            @Override
            public void run() {
                updateTree();
            }
        };

        toolBarManager.add(update);

        autoUpdate = new Action(Images.AUTOUPDATE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.AUTOUPDATE))) {
            
            @Override
            public void run() {
                if (autoUpdate.isChecked()) {
                    autoUpdateMode = true;
                    display.timerExec(1000, timer);
                } else {
                    autoUpdateMode = false;
                }
            }
        };

        autoUpdate.setChecked(true);
        toolBarManager.add(autoUpdate);

        toolBarManager.add(new Separator());

        onlyBlocked = new Action(Images.VIEW_ONLY_BLOCKED.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.VIEW_ONLY_BLOCKED))) {
            
            @Override
            public void run() {
                if (onlyBlocked.isChecked()) {
                    onlyBlockedMode = true;
                } else {
                    onlyBlockedMode = false;
                }
                updateTree();
            }
        };

        onlyBlocked.setChecked(false);
        toolBarManager.add(onlyBlocked);

        Action exportBlocks = new Action(Images.EXPORT_BLOCKS.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.EXPORT_BLOCKS))) {
            
            @Override
            public void run() {
                if (processTreeMap.keySet().stream()
                        .filter(dbcData -> dbcData.getStatus() == DbcStatus.BLOCKED).count() > 0) {
                    
                    BlocksHistory.getInstance().save(processTreeMap);
                    LOG.info("Блокировка сохранена...");
                } else {
                    LOG.info("Не найдено блокировок для сохранения");
                }
            }
        };

        toolBarManager.add(exportBlocks);

        Action importBlocks = new Action(Images.IMPORT_BLOCKS.getDescription()) {
            @Override
            public void run() {
                FileDialog dialog = new FileDialog(shell);
                dialog.setFilterPath(PathBuilder.getInstance().getBlockHistoryDir().toString());
                dialog.setText("Открыть историю блокировок");
                dialog.setFilterExtensions(new String[]{"*.xml"});
                
                ConcurrentMap<DbcData, Process> map = BlocksHistory.getInstance().open(dialog.open());
                List<DbcData> blockedDbsData = new ArrayList<DbcData>();
                for (Map.Entry<DbcData, Process> entry : map.entrySet())
                {
                    bhMainTree.setInput(entry.getValue());
                    blockedDbsData.add(entry.getKey());
                }
                bhServersTable.setInput(blockedDbsData);
                
                bhMainTree.refresh();
                bhServersTable.refresh();
            }
        };

        importBlocks.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.IMPORT_BLOCKS)));
        toolBarManager.add(importBlocks);

        toolBarManager.add(new Separator());
        
        Action settingsAction = new Action(Images.SETTINGS.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.SETTINGS))) {
            
            @Override
            public void run() {
                SettingsDlg settingsDlg = new SettingsDlg(shell, settings);
                settingsDlg.open();
            }
        };

        toolBarManager.add(settingsAction);
        
        return toolBarManager;
    }

    private Image getImage(Images type) {
        Image image = imagesMap.get(type.toString());
        if (image == null) {
            image = new Image(null, getClass().getClassLoader().getResourceAsStream(type.getImageAddr()));
            imagesMap.put(type.toString(), image);
        }
        return image;
    }
    
    private String getAppVersion() {
        URL manifestPath = MainForm.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
        Manifest manifest = null;
        try {
            manifest = new Manifest(manifestPath.openStream());
        } catch (IOException e) {
            LOG.error("Ошибка при чтении манифеста", e);
        }
        Attributes manifestAttributes = manifest.getMainAttributes();
        String appVersion = manifestAttributes.getValue("Implementation-Version");
        if(appVersion == null) {
            return "";
        }
        return appVersion;
    }
    
    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            if (autoUpdateMode) {
                updateTree();
                display.timerExec(settings.getUpdatePeriod() * 1000, this);
            }
        }
    };
    
    public void terminate(Process process) {
        String term = "select pg_terminate_backend(?);";
        boolean kill = false;
        int pid = process.getPid();
        try (PreparedStatement termPs = selectedDbcData.getConnection().prepareStatement(term);) {
            termPs.setInt(1, pid);
            try (ResultSet resultSet = termPs.executeQuery()) {
                if (resultSet.next()) {
                    kill = resultSet.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            selectedDbcData.setStatus(DbcStatus.ERROR);
            LOG.error(selectedDbcData.getName() + " " + e.getMessage(), e);
        }
        if(kill) {
            LOG.info(selectedDbcData.getName() + PID + pid + " is terminated.");
        } else {
            LOG.info(selectedDbcData.getName() + PID + pid + " is terminated failed.");
        }
    }

    public void cancel(Process process) {
        String cancel = "select pg_cancel_backend(?);";
        int pid = process.getPid();
        boolean kill = false;
        try (PreparedStatement cancelPs = selectedDbcData.getConnection().prepareStatement(cancel);) {
            cancelPs.setInt(1, pid);
            try (ResultSet resultSet = cancelPs.executeQuery()) {
                if (resultSet.next()) {
                    kill = resultSet.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            selectedDbcData.setStatus(DbcStatus.ERROR);
            LOG.error(selectedDbcData.getName() + " " + e.getMessage(), e);
        }
        if(kill) {
            LOG.info(selectedDbcData.getName() + PID + pid + " is canceled.");
        } else {
            LOG.info(selectedDbcData.getName() + PID + pid + " is canceled failed.");
        }
    }
    
    private void updateTree() {
        new Thread() {
            public void run() {
                if (selectedDbcData != null) {
                    synchronized (selectedDbcData) {
                        if (selectedDbcData != null) {
                            if (onlyBlockedMode) {
                                updateProcces = getOnlyBlockedProcessTree(selectedDbcData);
                            } else {
                                updateProcces = getProcessTree(selectedDbcData);
                            }
                        }
                    }
                    try {
                        display.syncExec(new Runnable() {
                            @Override
                            public void run() {
                                caMainTree.setInput(updateProcces);
                                caMainTree.refresh();
                                bhMainTree.refresh();
                            }
                        }); 
                    } catch (SWTException e) {
                        LOG.error("Ошибка при отрисовке таблицы!", e);
                    }
                }
            }
        }.start();
    }
    
    private void dbcDataConnect() {
        synchronized (selectedDbcData) {
            selectedDbcData.connect();
            caServersTable.refresh();
            serversToolBarState();
        }
        updateTree();
        display.timerExec(1000, timer);
    }
    
    private void dbcDataDisconnect() {
        synchronized (selectedDbcData) {
            selectedDbcData.disconnect();
            caServersTable.refresh();
            disconnectState();
        }
        updateTree();
    }
    
    private void serversToolBarState() {
        if (selectedDbcData != null &&
                (selectedDbcData.getStatus() == DbcStatus.ERROR ||
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
    }
    
    private void disconnectState() {
        deleteDB.setEnabled(true);
        editDB.setEnabled(true);
        connectDB.setEnabled(true);
        disconnectDB.setEnabled(false);
    }
}
