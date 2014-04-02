package ru.taximaxim.pgsqlblocks.ui;


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
    private static final int SASH_WIDTH = 3;
    private static Logger log = Logger.getLogger(MainForm.class);
    private static MainForm mainForm;
    private String[] caMainTreeColsName = new String[]{
            "pid", "application_name", "datname", "usename", "client", "backend_start", "query_stat", 
            "xact_stat", "state", "state_change" , "blocked_by" , "query" , "slowquery"};
    private String[] caColName = {"PID", "APPLICATION_NAME", "DATNAME", "USENAME", "CLIENT", "BACKEND_START", "QUERY_STAT", 
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
        PID, APPLICATION_NAME, DATNAME, USENAME, CLIENT, BACKEND_START, QUERY_STAT, 
        XACT_STAT, STATE, STATE_CHANGE , BLOCKED_BY , QUERY , SLOWQUERY, DEFAULT;
    }
    private int[] caMainTreeColsSize = new int[]{80,150,100,100,100,100,100,100,100,100,80,150,80};
    private Shell shell;
    private ResHelper resHelper = ResHelper.getInstance();

    private List<DbcData> dbcList;
    private List<Process> processList;
    private ConcurrentHashMap<DbcData, TableItem> serversTiMap = new ConcurrentHashMap<DbcData, TableItem>();

    private Menu menu;
    private MenuItem serversMi;
    private Menu serversMenu;
    private MenuItem registerMi;
    private MenuItem processesMi;
    private Menu processesMenu;
    private MenuItem viewBlocksMi;
    private MenuItem infoMi;
    private Menu infoMenu;
    private MenuItem aboutMi;

    private ToolBar toolBar;
    private ToolItem addDb;
    private ToolItem update;
    private ToolItem autoUpdateTi;
    private Spinner timerSpinner;
    private ToolItem terminateProc;
    private ToolItem cancelProc;
    private ToolItem closeProcComposite;
    private Text procText;

    private Composite topComposite;
    private TabFolder tabPanel;
    private TabItem currentActivityTi;
    private SashForm currentActivitySf;
    private SashForm caTreeSf;
    private Table caServersTable;
    private MenuItem onServer;
    private MenuItem offServer;
    private MenuItem editServer;
    private MenuItem deleteServer;
    private Tree caMainTree;
    private Composite procComposite;
    private TabItem blocksHistoryTi;
    private SashForm blocksHistorySf;
    private Table bhServersTable;
    private Table bhMainTable;

    private SashForm verticalSf;
    private Composite logComposite;
    private Composite statusBar;

    private AddDbcDataDlg dbcDlg;
    private ConfirmDlg confirmDlg;
    private Menu caServerListContextMenu;

    private DbcDataList dl = DbcDataList.getInstance();
    private DbcData selectedDbc;
    private ConcurrentHashMap<DbcData, Provider> connectsMap = new ConcurrentHashMap<DbcData, Provider>();
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private boolean autoUpdate = true;
    private int timerInterval = 10;
    private Process selectedProcess;
    private List<Process> expandedProcesses = new ArrayList<Process>();
    private SortColumn sortColumn = SortColumn.DEFAULT;
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
            shell = null;
        }
    }

    public Shell getShell() {
        return shell;
    }

    private void createControls() {
        dbcDlg = new AddDbcDataDlg(shell);
        confirmDlg = new ConfirmDlg(shell);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = ZERO_MARGIN;
        gridLayout.marginWidth = ZERO_MARGIN;

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);

        menu = new Menu(shell, SWT.BAR);
        {
            //Меню серверов
            serversMi = new MenuItem(menu, SWT.CASCADE);
            serversMi.setText("Серверы");
            serversMenu = new Menu(shell, SWT.DROP_DOWN);
            serversMi.setMenu(serversMenu);
            {
                registerMi = new MenuItem(serversMenu, SWT.PUSH);
                registerMi.setText("Зарегистрировать");
            }
            //Меню процессов
            processesMi = new MenuItem(menu, SWT.CASCADE);
            processesMi.setText("Процессы");
            processesMenu = new Menu(shell, SWT.DROP_DOWN);
            processesMi.setMenu(processesMenu);
            {
                viewBlocksMi = new MenuItem(processesMenu, SWT.CHECK);
                viewBlocksMi.setText("Показывать блокировки");
            }

            infoMi = new MenuItem(menu, SWT.CASCADE);
            infoMi.setText("?");
            infoMenu = new Menu(shell, SWT.DROP_DOWN);
            infoMi.setMenu(infoMenu);
            {
                aboutMi = new MenuItem(infoMenu, SWT.CHECK);
                aboutMi.setText("Справка");
            }
        }
        shell.setMenuBar(menu);

        toolBar = new ToolBar(shell, SWT.RIGHT | SWT.FLAT);
        {
            addDb = new ToolItem(toolBar, SWT.PUSH);
            addDb.setImage(resHelper.setImage(shell, "images/add_16.png"));
            addDb.setToolTipText("Добавить БД");

            ToolItem sep = new ToolItem(toolBar, SWT.SEPARATOR);
            sep.setWidth(10);

            update = new ToolItem(toolBar, SWT.PUSH);
            update.setImage(resHelper.setImage(shell, "images/update_16.png"));
            update.setToolTipText("Обновить");
            update.addListener(SWT.Selection, new Listener(){
                @Override
                public void handleEvent(Event event) {
                    updateProcesses();
                }
            });
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
            sep.setWidth(10);

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
                                terminateProc.setText("terminate");

                                cancelProc = new ToolItem(pcToolBar, SWT.PUSH);
                                cancelProc.setText("cancel");

                                closeProcComposite = new ToolItem(pcToolBar, SWT.PUSH);
                                closeProcComposite.setText("Закрыть");

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
                        }
                        bhMainTable = new Table(blocksHistorySf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                        {
                            bhMainTable.setHeaderVisible(true);
                            bhMainTable.setLinesVisible(true);
                            bhMainTable.setLayoutData(gridData);
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

        statusBar = new Composite(shell, SWT.BORDER);
        {
            statusBar.setLayout(gridLayout);
            statusBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
            Label statusLabel = new Label(statusBar, SWT.HORIZONTAL);
            statusLabel.setText("statusbar");
        }
        caServerListContextMenu = new Menu(shell, SWT.POP_UP);
        {
            onServer = new MenuItem(caServerListContextMenu, SWT.PUSH);
            onServer.setText("Подключиться");
            offServer = new MenuItem(caServerListContextMenu, SWT.PUSH);
            offServer.setText("Отключиться");
            editServer = new MenuItem(caServerListContextMenu, SWT.PUSH);
            editServer.setText("Редактировать");
            deleteServer = new MenuItem(caServerListContextMenu, SWT.PUSH);
            deleteServer.setText("Удалить");
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
                    caMainTree.removeAll();
                    updateUI(selectedDbc);
                }
            });
        }
        addDb.addListener(SWT.Selection,new Listener() {
            @Override
            public void handleEvent(Event event) {
                dbcDlg.show();
            }
        });
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

        onServer.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                connect(selectedDbc);
            }
        });

        offServer.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                disconnect(selectedDbc);
            }
        });

        editServer.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                dbcDlg.show(selectedDbc);
            }
        });

        deleteServer.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                confirmDlg.show(selectedDbc);
            }
        });
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
        closeProcComposite.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                procCompositeHide();
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
        caMainTree.addListener(SWT.SetData, caMainTreeListener);
        caMainTree.addListener(SWT.Expand, caMainTreeListener);
        caMainTree.addListener(SWT.Selection, caMainTreeListener);
        BlocksHistory.getInstance().save();
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
                    if(process.equals(selectedProcess))
                        caMainTree.select(item);
                    if(process.getChildren().size()>0) {
                        item.setImage(resHelper.setImage(shell, "images/locker_16.png"));
                        item.setItemCount(process.getChildren().size());
                        if(expandedProcesses.contains(process)) {
                            item.setExpanded(true);
                        }
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
                    if(process.equals(selectedProcess))
                        caMainTree.select(item);
                    if(process.getChildren().size()>0) {
                        item.setImage(resHelper.setImage(shell, "images/locker_16.png"));
                        item.setItemCount(process.getChildren().size());
                        if(expandedProcesses.contains(process)) {
                            item.setExpanded(true);
                        }
                    } else {
                        item.setImage(resHelper.setImage(shell, "images/locked_16.png"));
                    }
                }
                break;
            }
        }
    };

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
        caMainTree.removeAll();
        executor.execute(connectsMap.get(selectedDbc).getProcesses);
    }

    private void cancel() {
        if(selectedDbc != null && selectedProcess != null) {
            connectsMap.get(selectedDbc).cancel(selectedProcess.getPid());
        }
        caMainTree.removeAll();
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
                executor.execute(map.getValue().connect);
            }
        }
    }

    public synchronized void serverStatusUpdate(DbcData dbc) {
        TableItem ti = serversTiMap.get(dbc);
        ti.setImage(resHelper.setImage(shell,dbc.getStatus().getImageAddr()));
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
        connectsMap.remove(dbc);
        serversTiMap.get(dbc).dispose();
        serversTiMap.remove(dbc);
    }

    public void connect(DbcData dbc) {
        executor.execute(connectsMap.get(dbc).connect);
    }

    public synchronized void updateUI(DbcData data) {
        if(data.equals(selectedDbc)) {
            for(TreeItem ti :caMainTree.getItems()) {
                ti.setItemCount(0);
            }
            caMainTree.clearAll(true);
            processList = connectsMap.get(data).getProcessList();
            if(processList != null) {
                Collections.sort(processList,new Comparator<Process>() {
                    @Override
                    public int compare(Process o1, Process o2) {
                        switch(sortColumn){
                        case DEFAULT:
                            return 0;
                        case PID:
                            if(o1.getChildren().size()>o2.getChildren().size())
                                return sortDirection == SortDirection.UP?1:-1;
                            if(o1.getChildren().size()<o2.getChildren().size())
                                return sortDirection == SortDirection.UP?-1:1;
                            return 0;
                        case APPLICATION_NAME:
                            return sortDirection == SortDirection.UP?
                                    o1.getApplicationName().compareTo(o2.getApplicationName()):
                                        o2.getApplicationName().compareTo(o1.getApplicationName());
                        case DATNAME:
                            return sortDirection == SortDirection.UP?
                                    o1.getDatname().compareTo(o2.getDatname()):
                                        o2.getDatname().compareTo(o1.getDatname());
                        case USENAME:
                            return sortDirection == SortDirection.UP?
                                    o1.getUsename().compareTo(o2.getUsename()):
                                        o2.getUsename().compareTo(o1.getUsename());
                        case CLIENT:
                            return sortDirection == SortDirection.UP?
                                    o1.getClient().compareTo(o2.getClient()):
                                        o2.getClient().compareTo(o1.getClient());
                        case BACKEND_START:
                            return 0;
                        case QUERY_STAT:
                            return 0;
                        case XACT_STAT:
                            return 0;
                        case STATE:
                            return 0;
                        case STATE_CHANGE:
                            return 0;
                        case BLOCKED_BY:
                            return 0;
                        case QUERY:
                            return 0;
                        case SLOWQUERY:
                            return 0;
                        default:
                            return 0;
                        }
                    }
                });
                caMainTree.setItemCount(processList.size());
            } else {
                caMainTree.setItemCount(0);
            }
        }
    }

    private void showSm(Point location) {
        if(selectedDbc == null) 
            return;
        boolean isConnected = connectsMap.get(selectedDbc).isConnected();
        onServer.setEnabled(!isConnected);
        offServer.setEnabled(isConnected);
        editServer.setEnabled(!isConnected);
        deleteServer.setEnabled(!isConnected);
        caServerListContextMenu.setLocation(location);
        caServerListContextMenu.setVisible(true);
    }

    public Display getDisplay() {
        return shell.getDisplay();
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

    private void setSelectedDbc(DbcData dbc) {
        if(!dbc.equals(selectedDbc)) {
            selectedProcess = null;
            procCompositeHide();
            selectedDbc = dbc;
        }
    }

    private void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
}


