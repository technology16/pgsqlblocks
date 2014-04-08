package ru.taximaxim.pgsqlblocks.ui;


import java.io.IOException;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import ru.taximaxim.pgsqlblocks.BlocksHistory;
import ru.taximaxim.pgsqlblocks.DbcData;
import ru.taximaxim.pgsqlblocks.DbcDataList;
import ru.taximaxim.pgsqlblocks.DbcStatus;
import ru.taximaxim.pgsqlblocks.Process;
import ru.taximaxim.pgsqlblocks.Provider;

public final class MainForm {

    private MainForm() {}
    public static final int BTN_WIDTH = 120;
    public static final int TEXT_WIDTH = 200;
    private static final String APP_NAME = "pgSqlBlocks";
    private static final int ZERO_MARGIN = 0;
    private static final int[] VERTICAL_WEIGHTS = new int[]{80,20};
    private static final int[] HORIZONTAL_WEIGHTS = new int[]{17,83};
    private static final int SASH_WIDTH = 2;
    private static Logger log = Logger.getLogger(MainForm.class);
    private static MainForm mainForm;
    private String[] caMainTreeColsName = new String[]{
            "pid","blocked_count", "application_name", "datname", "usename", "client", "backend_start", "query_start", 
            "xact_stat", "state", "state_change" , "blocked_by" , "query" , "slowquery"};
    private String[] caColName = {"PID","BLOCKED_COUNT", "APPLICATION_NAME", "DATNAME", "USENAME", "CLIENT", "BACKEND_START", "QUERY_START", 
            "XACT_STAT", "STATE", "STATE_CHANGE" , "BLOCKED_BY" , "QUERY" , "SLOWQUERY"};
    private enum SortDirection{
        UP,
        DOWN;
        public SortDirection getOpposite() {
            if(this == UP)
                return DOWN;
            return UP;
        }
        public int getSwtData(){
            if(this == UP)
                return SWT.UP;
            return SWT.DOWN;
        }
    }
    private enum SortColumn{
        PID, BLOCKED_COUNT, APPLICATION_NAME, DATNAME, USENAME, CLIENT, BACKEND_START, QUERY_START, 
        XACT_STAT, STATE, STATE_CHANGE , BLOCKED_BY , QUERY , SLOWQUERY, DEFAULT;
    }
    private int[] caMainTreeColsSize = new int[]{80,100,150,100,100,100,138,138,138,100,138,80,150,80};
    private Shell shell;
    private ResHelper resHelper = ResHelper.getInstance();

    private List<DbcData> dbcList;
    private List<Process> processList;
    private ConcurrentHashMap<DbcData, TableItem> serversTiMap = new ConcurrentHashMap<DbcData, TableItem>();
    private DbcDataList dl = DbcDataList.getInstance();
    private DbcData selectedDbc;
    private ConcurrentHashMap<DbcData, Provider> connectsMap = new ConcurrentHashMap<DbcData, Provider>();
    private ConcurrentHashMap<DbcData, List<Process>> historyMap;
    private List<Process> historyProcessList;
    private Process selectedProcess;
    private List<Process> expandedProcesses = new ArrayList<Process>();
    private String appVersion;
    private Label appVersionLabel;

    private ToolBar toolBar;
    private ToolItem addDbTi;
    private ToolItem removeDbTi;
    private ToolItem editDbTi;
    private ToolItem connectDbTi;
    private ToolItem disconnectDbTi;
    private ToolItem updateTi;
    private ToolItem autoUpdateTi;
    private Spinner timerSpinner;
    private ToolItem onlyBlockedTi;
    private ToolItem saveBlocksHistory;
    private ToolItem openBlocksHistory;

    private ToolItem terminateProc;
    private ToolItem cancelProc;
    private Text procText;

    private Composite topComposite;
    private TabFolder tabPanel;
    private TabItem currentActivityTi;
    private SashForm currentActivitySf;
    private SashForm caTreeSf;
    private Table caServersTable;
    private MenuItem connectDbCm;
    private MenuItem disconnectDbCm;
    private MenuItem editDbCm;
    private MenuItem removeDbCm;
    private Tree caMainTree;
    private Composite procComposite;
    private TabItem blocksHistoryTi;
    private SashForm blocksHistorySf;
    private Table bhServersTable;
    private Tree bhMainTree;

    private SashForm verticalSf;
    private Composite logComposite;
    private Composite statusBar;

    private AddDbcDataDlg dbcDlg;
    private ConfirmDlg confirmDlg;
    private Menu caServerListContextMenu;

    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private boolean autoUpdate = true;
    private boolean onlyBlocked = false;
    private int timerInterval = 10;
    private SortColumn sortColumn = SortColumn.BLOCKED_COUNT;
    private SortDirection sortDirection = SortDirection.UP;

    public final static MainForm getInstance() {
        if(mainForm == null) {
            mainForm = new MainForm();
        }
        return mainForm;
    }

    public void show(Display display) {
        try {
            shell = new Shell(display);
            shell.setText(APP_NAME);
            shell.setMaximized(true);
            shell.setLayout(new GridLayout());
            createControls();
            addHandlers();
            shell.open();
            UIAppender uiApp = new UIAppender(logComposite);
            uiApp.setThreshold(Level.INFO);
            Logger.getRootLogger().addAppender(uiApp);
            dbcListInit();
            getDisplay().timerExec(1000, timer);
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch(Exception e){
            log.error("Что-то пошло не так", e);
        }
        finally {
            executor.shutdown();
            Enumeration<Driver> driver = DriverManager.getDrivers();
            for(Entry<DbcData, Provider> map : connectsMap.entrySet()) {
                if(map.getValue().isConnected()) {
                    map.getValue().disconnect();
                }
            }
            while(driver.hasMoreElements()) {
                try {
                    DriverManager.deregisterDriver(driver.nextElement());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            BlocksHistory.getInstance().save();
            shell = null;
        }
    }


    private void createControls() {
        dbcDlg = new AddDbcDataDlg(shell);
        confirmDlg = new ConfirmDlg(shell);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = ZERO_MARGIN;
        gridLayout.marginWidth = ZERO_MARGIN;

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);

        toolBar = new ToolBar(shell, SWT.RIGHT | SWT.FLAT);
        {
            addDbTi = new ToolItem(toolBar, SWT.PUSH);
            addDbTi.setImage(resHelper.setImage(shell, "images/db_add_16.png"));
            addDbTi.setToolTipText("Добавить БД");

            removeDbTi = new ToolItem(toolBar, SWT.PUSH);
            removeDbTi.setImage(resHelper.setImage(shell, "images/db_del_16.png"));
            removeDbTi.setToolTipText("Удалить БД");
            removeDbTi.setEnabled(false);

            editDbTi = new ToolItem(toolBar, SWT.PUSH);
            editDbTi.setImage(resHelper.setImage(shell, "images/db_edit_16.png"));
            editDbTi.setToolTipText("Редактировать БД");
            editDbTi.setEnabled(false);

            ToolItem sep = new ToolItem(toolBar, SWT.SEPARATOR);
            sep.setWidth(10);

            connectDbTi = new ToolItem(toolBar, SWT.PUSH);
            connectDbTi.setImage(resHelper.setImage(shell, "images/db_connect_16.png"));
            connectDbTi.setToolTipText("Подключиться");
            connectDbTi.setEnabled(false);

            disconnectDbTi = new ToolItem(toolBar, SWT.PUSH);
            disconnectDbTi.setImage(resHelper.setImage(shell, "images/db_disconnect_16.png"));
            disconnectDbTi.setToolTipText("Отключиться");
            disconnectDbTi.setEnabled(false);

            sep = new ToolItem(toolBar, SWT.SEPARATOR);
            sep.setWidth(15);

            updateTi = new ToolItem(toolBar, SWT.PUSH);
            updateTi.setImage(resHelper.setImage(shell, "images/update_16.png"));
            updateTi.setToolTipText("Обновить");

            autoUpdateTi = new ToolItem(toolBar, SWT.CHECK);
            autoUpdateTi.setImage(resHelper.setImage(shell, "images/autoupdate_16.png"));
            autoUpdateTi.setToolTipText("Автообновление");
            autoUpdateTi.setSelection(autoUpdate);

            timerSpinner = new Spinner(toolBar,SWT.BORDER | SWT.READ_ONLY);
            timerSpinner.setMinimum(1);
            timerSpinner.setMaximum(100);
            timerSpinner.setSelection(timerInterval);
            timerSpinner.setToolTipText("Интервал для автообновления(сек)");
            timerSpinner.pack();
            ToolItem timerSpinnerSep = new ToolItem(toolBar, SWT.SEPARATOR);
            timerSpinnerSep.setControl(timerSpinner);
            timerSpinnerSep.setWidth(60);

            sep = new ToolItem(toolBar, SWT.SEPARATOR);
            sep.setWidth(15);
            
            onlyBlockedTi = new ToolItem(toolBar, SWT.CHECK);
            onlyBlockedTi.setImage(resHelper.setImage(shell, "images/db_ob_16.png"));
            onlyBlockedTi.setToolTipText("Показывать только блокирующие и блокированные процессы");
            onlyBlockedTi.setSelection(onlyBlocked);

            saveBlocksHistory = new ToolItem(toolBar, SWT.PUSH);
            saveBlocksHistory.setImage(resHelper.setImage(shell, "images/save_16.png"));
            saveBlocksHistory.setToolTipText("Выгрузить историю блокировок");

            openBlocksHistory = new ToolItem(toolBar, SWT.PUSH);
            openBlocksHistory.setImage(resHelper.setImage(shell, "images/document_open_16.png"));
            openBlocksHistory.setToolTipText("Открыть файл с историей блокировок");
        }

        verticalSf = new SashForm(shell, SWT.VERTICAL);
        {
            verticalSf.setLayout(gridLayout);
            verticalSf.setLayoutData(gridData);
            verticalSf.SASH_WIDTH = SASH_WIDTH;
            verticalSf.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

            topComposite = new Composite(verticalSf, SWT.NONE);
            topComposite.setLayout(gridLayout);

            tabPanel = new TabFolder(topComposite, SWT.BORDER);
            {
                tabPanel.setLayoutData(gridData);
                currentActivityTi = new TabItem(tabPanel, SWT.NONE);
                {
                    currentActivityTi.setText("Текущая активность");
                    currentActivitySf = new SashForm(tabPanel, SWT.HORIZONTAL);
                    {
                        currentActivitySf.setLayout(gridLayout);
                        currentActivitySf.setLayoutData(gridData);
                        currentActivitySf.SASH_WIDTH = SASH_WIDTH;
                        currentActivitySf.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

                        caServersTable = new Table(currentActivitySf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                        {
                            caServersTable.setHeaderVisible(true);
                            caServersTable.setLinesVisible(true);
                            caServersTable.setLayoutData(gridData);
                            TableColumn serversTc = new TableColumn(caServersTable, SWT.NONE);
                            serversTc.setText("Сервер");
                            serversTc.setWidth(200);
                        }
                        caTreeSf = new SashForm(currentActivitySf, SWT.VERTICAL);
                        {
                            caMainTree = new Tree(caTreeSf, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                            {
                                caMainTree.setHeaderVisible(true);
                                caMainTree.setLinesVisible(true);
                                caMainTree.setLayoutData(gridData);
                                for(int i=0;i<caMainTreeColsName.length;i++) {
                                    TreeColumn treeColumn = new TreeColumn(caMainTree, SWT.NONE);
                                    treeColumn.setText(caMainTreeColsName[i]);
                                    treeColumn.setData("colName",caColName[i]);
                                    SortDirection sortDirection = SortDirection.UP;
                                    treeColumn.setData("sortDirection", sortDirection);
                                    treeColumn.setWidth(caMainTreeColsSize[i]);
                                }
                            }
                            procComposite = new Composite(caTreeSf, SWT.BORDER);
                            {
                                procComposite.setLayout(gridLayout);
                                GridData procCompositeGd = new GridData(SWT.FILL, SWT.FILL, true, true);
                                procComposite.setLayoutData(procCompositeGd);
                                procComposite.setVisible(false);

                                ToolBar pcToolBar = new ToolBar(procComposite, SWT.FLAT | SWT.RIGHT);
                                pcToolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

                                terminateProc = new ToolItem(pcToolBar, SWT.PUSH);
                                terminateProc.setText("Уничтожить процесс");

                                cancelProc = new ToolItem(pcToolBar, SWT.PUSH);
                                cancelProc.setText("Послать сигнал отмены процесса");

                                procText = new Text(procComposite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
                                procText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                                procText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
                            }
                        }
                        caTreeSf.setWeights(VERTICAL_WEIGHTS);
                    }
                    currentActivitySf.setWeights(HORIZONTAL_WEIGHTS);
                    currentActivityTi.setControl(currentActivitySf);
                }
                blocksHistoryTi = new TabItem(tabPanel, SWT.NONE);
                {
                    blocksHistoryTi.setText("История блокировок");
                    blocksHistorySf = new SashForm(tabPanel, SWT.HORIZONTAL);
                    {
                        blocksHistorySf.setLayout(gridLayout);
                        blocksHistorySf.setLayoutData(gridData);
                        blocksHistorySf.SASH_WIDTH = SASH_WIDTH;
                        blocksHistorySf.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

                        bhServersTable = new Table(blocksHistorySf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                        {
                            bhServersTable.setHeaderVisible(true);
                            bhServersTable.setLinesVisible(true);
                            bhServersTable.setLayoutData(gridData);
                            TableColumn serversTc = new TableColumn(bhServersTable, SWT.NONE);
                            serversTc.setText("Сервер");
                            serversTc.setWidth(200);
                        }
                        bhMainTree = new Tree(blocksHistorySf, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                        {
                            bhMainTree.setHeaderVisible(true);
                            bhMainTree.setLinesVisible(true);
                            bhMainTree.setLayoutData(gridData);
                            for(int i=0;i<caMainTreeColsName.length;i++) {
                                TreeColumn treeColumn = new TreeColumn(bhMainTree, SWT.NONE);
                                treeColumn.setText(caMainTreeColsName[i]);
                                treeColumn.setWidth(caMainTreeColsSize[i]);
                            }
                        }
                    }
                    blocksHistorySf.setWeights(HORIZONTAL_WEIGHTS);
                    blocksHistoryTi.setControl(blocksHistorySf);
                }
            }
            logComposite = new Composite(verticalSf, SWT.NONE);
            {
                logComposite.setLayout(gridLayout);
            }
            verticalSf.setWeights(VERTICAL_WEIGHTS);
        }

        statusBar = new Composite(shell, SWT.NONE);
        {
            statusBar.setLayout(gridLayout);
            statusBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
            appVersionLabel = new Label(statusBar, SWT.HORIZONTAL);
        }
        caServerListContextMenu = new Menu(shell, SWT.POP_UP);
        {
            connectDbCm = new MenuItem(caServerListContextMenu, SWT.PUSH);
            connectDbCm.setText("Подключиться");
            disconnectDbCm = new MenuItem(caServerListContextMenu, SWT.PUSH);
            disconnectDbCm.setText("Отключиться");
            editDbCm = new MenuItem(caServerListContextMenu, SWT.PUSH);
            editDbCm.setText("Редактировать");
            removeDbCm = new MenuItem(caServerListContextMenu, SWT.PUSH);
            removeDbCm.setText("Удалить");
        }
    }

    private void addHandlers() {
        for(final TreeColumn tc : caMainTree.getColumns()) {
            tc.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    caMainTree.setSortColumn(tc);
                    tc.setData("sortDirection", ((SortDirection)tc.getData("sortDirection")).getOpposite());
                    sortDirection = (SortDirection)tc.getData("sortDirection");
                    caMainTree.setSortDirection(sortDirection.getSwtData());
                    sortColumn = SortColumn.valueOf((String)tc.getData("colName"));
                    updateUI(selectedDbc);
                }
            });
        }

        caServersTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(MouseEvent e) {
                Point point = new Point(e.x , e.y);
                TableItem ti = caServersTable.getItem(point);
                if(ti==null)
                    return;
                if(e.stateMask == SWT.BUTTON3) {
                    setSelectedDbc((DbcData)ti.getData());
                    showSm(caServersTable.toDisplay(point));
                }
                if(e.stateMask == SWT.BUTTON1) {
                    setSelectedDbc((DbcData)ti.getData());
                    updateUI(selectedDbc);
                }
            }
            @Override
            public void mouseDown(MouseEvent e) {
            }
            @Override
            public void mouseDoubleClick(MouseEvent e) {}
        });
        bhServersTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(MouseEvent e) {
                Point point = new Point(e.x , e.y);
                TableItem ti = bhServersTable.getItem(point);
                if(ti==null)
                    return;
                historyProcessList = historyMap.get((DbcData)ti.getData());
                if(historyProcessList == null)
                    return;
                bhMainTree.removeAll();
                bhMainTree.setItemCount(historyProcessList.size());
            }
            @Override
            public void mouseDoubleClick(MouseEvent e) {}
            @Override
            public void mouseDown(MouseEvent e) {}
        });

        connectDbCm.addListener(SWT.Selection, onServerListener);
        connectDbTi.addListener(SWT.Selection, onServerListener);
        disconnectDbCm.addListener(SWT.Selection, offServerListener);
        disconnectDbTi.addListener(SWT.Selection, offServerListener);
        editDbCm.addListener(SWT.Selection, editServerListener);
        editDbTi.addListener(SWT.Selection, editServerListener);
        removeDbCm.addListener(SWT.Selection, deleteServerListener);
        removeDbTi.addListener(SWT.Selection, deleteServerListener);
        addDbTi.addListener(SWT.Selection, addDbListener);

        timerSpinner.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event event){
                timerInterval = Integer.parseInt(((Spinner)event.widget).getText());
            }
        });
        autoUpdateTi.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setAutoUpdate(autoUpdateTi.getSelection());
            }
        });
        terminateProc.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                terminate();
            }
        });
        cancelProc.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                cancel();
            }
        });
        saveBlocksHistory.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                BlocksHistory.getInstance().save();
            }
        });
        openBlocksHistory.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                FileDialog fd = new FileDialog(shell);
                fd.setFilterPath("/BlocksHistory");
                fd.setText("Открыть историю блокировок");
                fd.setFilterExtensions(new String[]{"*.xml"});
                BlocksHistory.getInstance().open(fd.open());
            }
        });
        updateTi.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event event) {
                updateProcesses();
            }
        });
        caMainTree.addListener(SWT.SetData, caMainTreeListener);
        caMainTree.addListener(SWT.Expand, caMainTreeListener);
        caMainTree.addListener(SWT.Selection, caMainTreeListener);
        bhMainTree.addListener(SWT.SetData, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TreeItem item = (TreeItem)event.item;
                TreeItem parentItem = item.getParentItem();
                Process process = null;
                if(parentItem == null) {
                    process = historyProcessList.get(event.index);
                    item.setText(process.toTree());
                    item.setData(process);
                    if(process.getChildren().size()>0) {
                        item.setImage(resHelper.setImage(shell, "images/locker_16.png"));
                        item.setItemCount(process.getChildren().size());
                    } else {
                        item.setImage(resHelper.setImage(shell, "images/nb_16.png"));
                    }
                } else {
                    try{
                        process = ((Process)parentItem.getData()).getChildren().get(event.index);
                    } catch(IndexOutOfBoundsException e) {
                        log.error("Ошибка в построении дерева");
                        return;
                    }
                    item.setText(process.toTree());
                    item.setData(process);
                    if(process.getChildren().size()>0) {
                        item.setImage(resHelper.setImage(shell, "images/locker_16.png"));
                        item.setItemCount(process.getChildren().size());
                    } else {
                        item.setImage(resHelper.setImage(shell, "images/locked_16.png"));
                    }
                }
            }
        });
        appVersionLabel.setText("PgSqlBlocks v." + getAppVersion());
        onlyBlockedTi.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setOnlyBlocked(onlyBlockedTi.getSelection());
                updateUI(selectedDbc);
            }
        });
    }

    private String getAppVersion() {
        URL manifestPath = MainForm.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
        Manifest manifest = null;
        try {
            manifest = new Manifest(manifestPath.openStream());
        } catch (IOException e) {
            log.error("Ошибка при чтении манифеста", e);
        }
        Attributes manifestAttributes = manifest.getMainAttributes();
        appVersion = manifestAttributes.getValue("Implementation-Version");
        if(appVersion == null)
            return "";
        return appVersion;
    }

    private Listener addDbListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            dbcDlg.show();
        }
    };

    private Listener onServerListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            connect(selectedDbc);
        }
    };

    private Listener editServerListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            dbcDlg.show(selectedDbc);
        }
    };

    private Listener deleteServerListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            confirmDlg.show(selectedDbc);
        }
    };

    private Listener offServerListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            disconnect(selectedDbc);
        }
    };

    public void setHistoryMap(ConcurrentHashMap<DbcData, List<Process>> hm) {
        historyMap = hm;
        bhServersTable.removeAll();
        bhMainTree.removeAll();
        for(Entry<DbcData, List<Process>> map : historyMap.entrySet()) {
            TableItem ti = new TableItem(bhServersTable, SWT.NONE);
            ti.setText(map.getKey().getName() + " [" + map.getKey().getUrl() + "]");
            ti.setData(map.getKey());
        }
    }

    private Listener caMainTreeListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            switch(event.type) {
            case SWT.Expand:
                Process proc = (Process)event.item.getData();
                if(!expandedProcesses.contains(proc))
                    expandedProcesses.add((Process)event.item.getData());
                break;
            case SWT.Selection:
                TreeItem ti = (TreeItem)event.item;
                if(ti!=null) {
                    selectedProcess = (Process)ti.getData();
                    procCompositeShow(selectedProcess);
                }
                break;
            case SWT.SetData:
                TreeItem item = (TreeItem)event.item;
                TreeItem parentItem = item.getParentItem();
                Process process = null;
                if(parentItem == null) {
                    process = processList.get(event.index);
                    item.setText(process.toTree());
                    item.setData(process);
                    if(process.getChildren().size()>0) {
                        item.setImage(resHelper.setImage(shell, "images/locker_16.png"));
                        item.setItemCount(process.getChildren().size());
                        if(expandedProcesses.contains(process)) {
                            item.setExpanded(true);
                        }
                    } else {
                        item.setImage(resHelper.setImage(shell, "images/nb_16.png"));
                    }
                    if(process.equals(selectedProcess))
                        caMainTree.select(item);
                } else {
                    try{
                        process = ((Process)parentItem.getData()).getChildren().get(event.index);
                    } catch(IndexOutOfBoundsException e) {
                        log.error("Ошибка в построении дерева");
                        return;
                    }
                    item.setText(process.toTree());
                    item.setData(process);
                    if(process.getChildren().size()>0) {
                        item.setImage(resHelper.setImage(shell, "images/locker_16.png"));
                        item.setItemCount(process.getChildren().size());
                        if(expandedProcesses.contains(process)) {
                            item.setExpanded(true);
                        }
                    } else {
                        item.setImage(resHelper.setImage(shell, "images/locked_16.png"));
                    }
                    if(process.equals(selectedProcess))
                        caMainTree.select(item);
                }
                break;
            }
        }
    };

    public Shell getShell() {
        return shell;
    }

    public Display getDisplay() {
        return shell.getDisplay();
    }

    private void procCompositeShow(Process proc) {
        if(!procComposite.isVisible()) {
            procComposite.setVisible(true);
            caTreeSf.layout(true, true);
        }
        procText.setText(String.format("pid=%s\n%s",proc.getPid(), proc.getQuery()));
    }

    private void procCompositeHide() {
        procComposite.setVisible(false);
        caTreeSf.layout(true, true);
    }

    private void terminate() {
        if(selectedDbc != null && selectedProcess != null) {
            connectsMap.get(selectedDbc).terminate(selectedProcess.getPid());
        }
        executor.execute(connectsMap.get(selectedDbc).getProcesses);
    }

    private void cancel() {
        if(selectedDbc != null && selectedProcess != null) {
            connectsMap.get(selectedDbc).cancel(selectedProcess.getPid());
        }
        executor.execute(connectsMap.get(selectedDbc).getProcesses);
    }

    private void dbcListInit() {
        dl.init();
        serverListUpdate();
        autoConnect();
    }

    public void providersInit() {
        for(DbcData dbc : dbcList) {
            providerInit(dbc);
        }
    }

    private void providerInit(DbcData dbc) {
        if(connectsMap.containsKey(dbc))
            return;
        Provider provider = new Provider(dbc);
        connectsMap.put(dbc, provider);
    }

    private void autoConnect() {
        for(Entry<DbcData, Provider> map : connectsMap.entrySet()) {
            if(map.getKey().isEnabled()) {
                executor.execute(map.getValue().getConnection());
            }
        }
    }

    public synchronized void serverStatusUpdate(DbcData dbc) {
        TableItem ti = serversTiMap.get(dbc);
        ti.setImage(resHelper.setImage(shell,dbc.getStatus().getImageAddr()));
        if(dbc.equals(selectedDbc)) {
            dbControlsStat();
        }
    }

    public void serverListUpdate() {
        dbcList = dl.getList();
        for(DbcData dbc : dbcList) {
            if(serversTiMap.containsKey(dbc))
                continue;
            providerInit(dbc);
            TableItem ti = new TableItem(caServersTable, SWT.NONE);
            ti.setText(dbc.getName());
            ti.setData(dbc);
            ti.setImage(resHelper.setImage(shell, "images/db_f_16.png"));
            serversTiMap.put(dbc, ti);
            serverStatusUpdate(dbc);
        }
        log.info("Обновление списка серверов");
    }

    public void deleteServer(DbcData dbc) {
        selectedDbc = null;
        connectsMap.remove(dbc);
        if(serversTiMap.get(dbc)!=null) {
            serversTiMap.get(dbc).dispose();
            serversTiMap.remove(dbc);
        }
    }

    public void connect(DbcData dbc) {
        executor.execute(connectsMap.get(dbc).getConnection());
    }

    public synchronized void updateUI(DbcData dbc) {
        if(dbc == null) {
            return;
        }
        if(dbc.equals(selectedDbc)) {
            for(TreeItem ti :caMainTree.getItems()) {
                ti.setItemCount(0);
            }
            caMainTree.clearAll(true);
            processList = connectsMap.get(dbc).getProcessList();
            if(processList != null) {
                Collections.sort(processList,new Comparator<Process>() {
                    @Override
                    public int compare(Process o1, Process o2) {
                        switch(sortColumn){
                        case DEFAULT:
                            return 0;
                        case PID:
                            if(o1.getChildren().size()>o2.getChildren().size())
                                return sortDirection == SortDirection.UP?-1:1;
                            if(o1.getChildren().size()<o2.getChildren().size())
                                return sortDirection == SortDirection.UP?1:-1;
                            return 0;
                        case BLOCKED_COUNT:
                            if(o1.getChildrensCount()>o2.getChildrensCount())
                                return sortDirection == SortDirection.UP?-1:1;
                            if(o1.getChildrensCount()<o2.getChildrensCount())
                                return sortDirection == SortDirection.UP?1:-1;
                            return 0;
                        case APPLICATION_NAME:
                            return stringCompare(o1.getApplicationName(), o2.getApplicationName());
                        case DATNAME:
                            return stringCompare(o1.getDatname(), o2.getDatname());
                        case USENAME:
                            return stringCompare(o1.getUsename(), o2.getUsename());
                        case CLIENT:
                            return stringCompare(o1.getClient(), o2.getClient());
                        case BACKEND_START:
                            return stringCompare(o1.getBackendStart(), o2.getBackendStart());
                        case QUERY_START:
                            return stringCompare(o1.getQueryStart(), o2.getQueryStart());
                        case XACT_STAT:
                            return stringCompare(o1.getXactStart(), o2.getXactStart());
                        case STATE:
                            return stringCompare(o1.getState(), o2.getState());
                        case STATE_CHANGE:
                            return stringCompare(o1.getStateChange(), o2.getStateChange());
                        case BLOCKED_BY:
                            return 0;
                        case QUERY:
                            return stringCompare(o1.getQuery(), o2.getQuery());
                        case SLOWQUERY:
                            if(sortDirection == SortDirection.UP) {
                                if(o1.isSlowQuery() && o2.isSlowQuery())
                                    return 0;
                                if(o1.isSlowQuery() && !o2.isSlowQuery()) 
                                    return 1;
                                if(!o1.isSlowQuery() && o2.isSlowQuery())
                                    return -1;
                                return 0;
                            } else {
                                if(o1.isSlowQuery() && o2.isSlowQuery())
                                    return 0;
                                if(!o1.isSlowQuery() && o2.isSlowQuery()) 
                                    return 1;
                                if(o1.isSlowQuery() && !o2.isSlowQuery())
                                    return -1;
                                return 0;
                            }
                        default:
                            return 0;
                        }
                    }
                });
                if(onlyBlocked) {
                    List<Process> sep = new ArrayList<Process>();
                    for(Process process : processList) {
                        if(process.getChildren().size() > 0) {
                            sep.add(process);
                        }
                    }
                    processList = sep;
                }
                caMainTree.setItemCount(processList.size());
            } else {
                caMainTree.setItemCount(0);
            }
        }
    }

    private int stringCompare(String s1, String s2) {
        return sortDirection == SortDirection.DOWN?
                s1.compareTo(s2):s2.compareTo(s1);
    }

    private void dbControlsStat() {
        boolean isConnected = selectedDbc.getStatus() == DbcStatus.CONNECTED || selectedDbc.getStatus() == DbcStatus.BLOCKED;
        connectDbTi.setEnabled(!isConnected);
        disconnectDbTi.setEnabled(isConnected);
        connectDbCm.setEnabled(!isConnected);
        disconnectDbCm.setEnabled(isConnected);
        editDbCm.setEnabled(!isConnected);
        editDbTi.setEnabled(!isConnected);
        removeDbCm.setEnabled(!isConnected);
        removeDbTi.setEnabled(!isConnected);
    }

    private void showSm(Point location) {
        if(selectedDbc == null) 
            return;
        caServerListContextMenu.setLocation(location);
        dbControlsStat();
        caServerListContextMenu.setVisible(true);
    }

    private void setSelectedDbc(DbcData dbc) {
        if(!dbc.equals(selectedDbc)) {
            selectedProcess = null;
            procCompositeHide();
            selectedDbc = dbc;
            dbControlsStat();
        }
    }


    public void updateProcesses() {
        for(Entry<DbcData, Provider> map : connectsMap.entrySet()) {
            executor.execute(map.getValue().getProcesses);
        }
    }

    public void disconnect(DbcData dbc) {
        if(connectsMap.get(dbc) == null || !connectsMap.get(dbc).isConnected())
            return;
        connectsMap.get(dbc).disconnect();
    }

    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            if(autoUpdate){
                updateProcesses();
                getDisplay().timerExec(timerInterval * 1000, this);
            }
        }
    };
    private void setOnlyBlocked(boolean onlyBlocked) {
        this.onlyBlocked = onlyBlocked;
    }
    private void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        getDisplay().timerExec(1000, timer);
    }
}
