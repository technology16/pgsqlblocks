package ru.taximaxim.pgsqlblocks;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


public class MainForm extends ApplicationWindow {


    private ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<String, Image>();
    private static final Logger LOG = Logger.getLogger(MainForm.class);
    
    private static Display display;
    private static Shell shell;


    private static final String APP_NAME = "pgSqlBlocks";
    
    private static final String SORT_DIRECTION = "sortDirection";
    private static final String COL_NAME = "colName";
    private static final String LOCKER = "images/locker_16.png";
    private static final String LOCKED = "images/locked_16.png";
    private static final String BLOCKED = "images/blocked_16.png";
    private static final String BLOCKING = "images/blocking_16.png";
    private static final String NB = "images/nb_16.png";
    private static final int ZERO_MARGIN = 0;
    private static final int FIXEDTHREADPOOL = 5;
    private static final int TEN = 10;
    private static final int[] VERTICAL_WEIGHTS = new int[]{80,20};
    private static final int[] HORIZONTAL_WEIGHTS = new int[]{17,83};
    private static final int SASH_WIDTH = 2;
    
    private DbcDataList dbcDataList = DbcDataList.getInstance();
    private DbcData selectedDbcData;
    
    
    private List<DbcData> dbcList;
    private List<Process> processList;
    private ConcurrentMap<DbcData, TableItem> serversTiMap;
    private ConcurrentMap<DbcData, List<Process>> historyMap;
    private List<Process> historyProcessList;
    private Process selectedProcess;
    private Label appVersionLabel;
    private ToolItem terminateProc;
    private ToolItem cancelProc;
    private Text procText;
    private SashForm caTreeSf;
    private TableViewer caServersTable;
    private TreeViewer caMainTree;
    private Composite procComposite;
    private TableViewer bhServersTable;
    private TreeViewer bhMainTree;
    private Composite logComposite;
    
    private ToolBarManager toolBarManager;
    private Action addDb;
    private Action deleteDB;
    private Action editDB;
    private Action connectDB;
    private Action disconnectDB;
    private Action update;
    private Action autoUpdate;
    private Action onlyBlocked;
    private Action exportBlocks;
    private Action importBlocks;

    private AddDbcDataDlg addDbcDlg;
    private AddDbcDataDlg editDbcDlg;
    
    //private int timerInterval = TEN;
    private int timerInterval = 5;
    private boolean autoUpdateMode = true;
    private boolean onlyBlockedMode = false;
    
    private int[] caMainTreeColsSize = new int[]{80,110,150,110,110,110,145,145,145,55,145,70,65,150,80};
    private String[] caMainTreeColsName = new String[]{
            "pid", "blocked_count", "application_name", "datname", "usename", "client", "backend_start", "query_start",
            "xact_stat", "state", "state_change", "blocked", "waiting", "query" , "slowquery"};
    private String[] caColName = {"PID", "BLOCKED_COUNT", "APPLICATION_NAME", "DATNAME", "USENAME", "CLIENT", "BACKEND_START", "QUERY_START",
            "XACT_STAT", "STATE", "STATE_CHANGE", "BLOCKED", "WAITING", "QUERY", "SLOWQUERY"};
    
    public static final int BTN_WIDTH = 120;
    public static final int TEXT_WIDTH = 200;
    
    private enum SortDirection {
        UP,
        DOWN;
        
        public SortDirection getOpposite() {
            if(this == UP) {
                return DOWN;
            }
            return UP;
        }
        
        public int getSwtData() {
            if(this == UP) {
                return SWT.UP;
            }
            return SWT.DOWN;
        }
    }

    
    
    private ConcurrentMap<DbcData, ProcessTree> processTreeMap = new ConcurrentHashMap<>();
    private ConcurrentMap<DbcData, ProcessTree> blockedProcessTreeMap = new ConcurrentHashMap<>();
    
    public Process getProcessTree(DbcData dbcData) {
        ProcessTree processTree = processTreeMap.get(dbcData);
        if(processTree == null){
            processTree = new ProcessTree(dbcData);
            processTreeMap.put(dbcData, processTree);
        }
        
        return processTree.getProcessTree();
    }
    
    public Process getOnlyBlockedProcessTree(DbcData dbcData) {
        ProcessTree processTree = blockedProcessTreeMap.get(dbcData);
        if(processTree == null){
            processTree = new ProcessTree(dbcData);
            blockedProcessTreeMap.put(dbcData, processTree);
        }
        
        return processTree.getOnlyBlockedProcessTree();
    }
    
    

    public static void main(String[] args) {
        try {
            display = new Display();
            shell = new Shell(display);
            MainForm wwin = new MainForm(shell);
            wwin.setBlockOnOpen(true);
            wwin.open();
            Display.getCurrent().dispose();
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
                            dbcDataList.init();
                            caServersTable.setInput(dbcDataList.getList());
                            
                            caServersTable.refresh();
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
                            }
                            caMainTree.setContentProvider(new ProcessTreeContentProvider());
                            caMainTree.setLabelProvider(new ProcessTreeLabelProvider());
                            
                            //caMainTree.setInput(proccesTreeMap.getProcessTreeMap().get(new DbcData("name", "host", "port", "dbname", "user", "passwd", false)));
                            //caMainTree.setInput(new ProcessTree());
                            
                           /* caMainTree = new Tree(caTreeSf, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                            {
                                caMainTree.setHeaderVisible(true);
                                caMainTree.setLinesVisible(true);
                                caMainTree.setLayoutData(gridData);
                                for(int i=0;i<caMainTreeColsName.length;i++) {
                                    TreeColumn treeColumn = new TreeColumn(caMainTree, SWT.NONE);
                                    treeColumn.setText(caMainTreeColsName[i]);
                                    treeColumn.setData(COL_NAME,caColName[i]);
                                    treeColumn.setData(SORT_DIRECTION, SortDirection.UP);
                                    treeColumn.setWidth(caMainTreeColsSize[i]);
                                }
                            }*/
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
                                terminateProc.addListener(SWT.Selection, event -> {
                                    if (selectedProcess != null) {
                                        terminate(selectedProcess);
                                        updateTree();
                                        caServersTable.refresh();
                                    }
                                });
                                
                                cancelProc = new ToolItem(pcToolBar, SWT.PUSH);
                                cancelProc.setText("Послать сигнал отмены процесса");
                                cancelProc.addListener(SWT.Selection, event -> {
                                    if (selectedProcess != null) {
                                        cancel(selectedProcess);
                                        updateTree();
                                        caServersTable.refresh();
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
                            
                            /*bhServersTable.setContentProvider();
                            bhServersTable.setLabelProvider();
                            bhServersTable.setInput();*/
                            
                            bhServersTable.refresh();
                            
                            /*TableColumn serversTc = new TableColumn(bhServersTable, SWT.NONE);
                            serversTc.setText("Сервер");
                            serversTc.setWidth(200);*/
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
                            
                            /*bhMainTree.setContentProvider();
                            bhMainTree.setLabelProvider();*/
                            
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
            UIAppender uiAppender = new UIAppender(logComposite);
            uiAppender.setThreshold(Level.INFO);
            Logger.getRootLogger().addAppender(uiAppender);
            
            Composite statusBar = new Composite(composite, SWT.NONE);
            {
                statusBar.setLayout(gridLayout);
                statusBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
                appVersionLabel = new Label(statusBar, SWT.HORIZONTAL);
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
                    if (selectedDbcData.getStatus() == DbcStatus.ERROR ||
                            selectedDbcData.getStatus() == DbcStatus.DISABLED) {
                        
                        deleteDB.setEnabled(true);
                        editDB.setEnabled(true);
                        connectDB.setEnabled(true);
                        disconnectDB.setEnabled(false);
                    } else {
                        deleteDB.setEnabled(false);
                        editDB.setEnabled(false);
                        connectDB.setEnabled(false);
                        disconnectDB.setEnabled(true);
                    }
                    if (selectedDbcData != null) {
                        updateTree();
                    }
                    caMainTree.refresh();
                    caServersTable.refresh();
                }
            }
        });
        
        return parent;
    }

    protected ToolBarManager createToolBarManager(int style) {
        toolBarManager = new ToolBarManager(style);

        addDb = new Action(Images.ADD_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.ADD_DATABASE))) {
            
            @Override
            public void run() {
                addDbcDlg.open();
                caServersTable.refresh();
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
                    dbcDataList.delete(selectedDbcData);
                }
                caServersTable.refresh();
            }
        };

        deleteDB.setEnabled(false);
        toolBarManager.add(deleteDB);

        editDB = new Action(Images.EDIT_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.EDIT_DATABASE))) {
            
            @Override
            public void run() {
                editDbcDlg = new AddDbcDataDlg(shell, selectedDbcData);
                editDbcDlg.open();
                caServersTable.refresh();
            }
        };

        editDB.setEnabled(false);
        toolBarManager.add(editDB);

        // Add a separator
        toolBarManager.add(new Separator());

        connectDB = new Action(Images.CONNECT_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.CONNECT_DATABASE))) {
            
            @Override
            public void run() {
               selectedDbcData.connect();
               
               deleteDB.setEnabled(false);
               editDB.setEnabled(false);
               connectDB.setEnabled(false);
               disconnectDB.setEnabled(true);
               
               updateTree();
               caServersTable.refresh();
               display.timerExec(1000, timer);
            }
        };

        connectDB.setEnabled(false);
        toolBarManager.add(connectDB);

        disconnectDB = new Action(Images.DISCONNECT_DATABASE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.DISCONNECT_DATABASE))) {
            
            @Override
            public void run() {
                selectedDbcData.disconnect();
                
                deleteDB.setEnabled(true);
                editDB.setEnabled(true);
                connectDB.setEnabled(true);
                disconnectDB.setEnabled(false);
                
                updateTree();
                caServersTable.refresh();
            }
        };

        disconnectDB.setEnabled(false);
        toolBarManager.add(disconnectDB);

        // Add a separator
        toolBarManager.add(new Separator());

        update = new Action(Images.UPDATE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.UPDATE))) {
            
            @Override
            public void run() {
                updateTree();
                caServersTable.refresh();
            }
        };

        toolBarManager.add(update);

        autoUpdate = new Action(Images.AUTOUPDATE.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.AUTOUPDATE))) {
            
            @Override
            public void run() {
                if (autoUpdate.isChecked()) {
                    autoUpdateMode = true;
                } else {
                    autoUpdateMode = false;
                }
            }
        };

        autoUpdate.setChecked(true);
        toolBarManager.add(autoUpdate);

        ///////////////////////////////
        
        // Вставить Spinner
        
       /* Spinner timerSpinner = new Spinner(tb, SWT.BORDER | SWT.READ_ONLY);
        timerSpinner.setMinimum(1);
        timerSpinner.setMaximum(100);
        timerSpinner.setSelection(timerInterval);
        timerSpinner.setToolTipText("Интервал для автообновления(сек)");*/
      //  timerSpinner.pack();
        
        /*ToolItem timerSpinnerSep = new ToolItem(toolBar, SWT.SEPARATOR);
        timerSpinnerSep.setControl(timerSpinner);
        timerSpinnerSep.setWidth(60);*/

        //////   TimerSpinner   //////

        //////////////////////////////

        toolBarManager.add(new Separator());

        onlyBlocked = new Action(Images.VIEW_ONLY_BLOCKED.getDescription(),
                ImageDescriptor.createFromImage(getImage(Images.VIEW_ONLY_BLOCKED))) {
            
            @Override
            public void run() {
                if (onlyBlocked.isChecked()) {
                    onlyBlockedMode = true;
                    updateTree();
                } else {
                    onlyBlockedMode = false;
                }
            }
        };

        onlyBlocked.setChecked(false);
        toolBarManager.add(onlyBlocked);

        exportBlocks = new Action(Images.EXPORT_BLOCKS.getDescription(),
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

        importBlocks = new Action(Images.IMPORT_BLOCKS.getDescription()) {
            @Override
            public void run() {
                FileDialog dialog = new FileDialog(shell);
                dialog.setFilterPath(System.getProperty("user.home") + "/BlocksHistory");
                dialog.setText("Открыть историю блокировок");
                dialog.setFilterExtensions(new String[]{"*.xml"});
                
                ConcurrentMap<DbcData, Process> map = BlocksHistory.getInstance().open(dialog.open());
                List<DbcData> ll = new ArrayList<DbcData>();
                for (DbcData key : map.keySet()) {
                    bhMainTree.setInput(map.get(key));
                    ll.add(key);
                }
                bhServersTable.setInput(ll);
                
                bhMainTree.refresh();
                bhServersTable.refresh();
            }
        };

        importBlocks.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.IMPORT_BLOCKS)));
        toolBarManager.add(importBlocks);

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
                display.timerExec(timerInterval * 1000, this);
            }
            updateTree();
            caServersTable.refresh();
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
            LOG.info(selectedDbcData.getName() + " pid=" + pid + " is terminated.");
        } else {
            LOG.info(selectedDbcData.getName() + " pid=" + pid + " is terminated failed.");
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
            LOG.info(selectedDbcData.getName() + " pid=" + pid + " is canceled.");
        } else {
            LOG.info(selectedDbcData.getName() + " pid=" + pid + " is canceled failed.");
        }
    }
    
    private void updateTree() {
        if (selectedDbcData != null) {
            if (onlyBlockedMode) {
                caMainTree.setInput(getOnlyBlockedProcessTree(selectedDbcData));
            } else {
                caMainTree.setInput(getProcessTree(selectedDbcData));
            }
        }
        caMainTree.refresh();
    }
}

