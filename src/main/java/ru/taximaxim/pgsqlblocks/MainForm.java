package ru.taximaxim.pgsqlblocks;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
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


public class MainForm extends ApplicationWindow {


    private ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<String, Image>();


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
    private Process viewProcessTree;
    

    private ExecutorService executor = Executors.newFixedThreadPool(FIXEDTHREADPOOL);
    
    private List<DbcData> dbcList;
    private List<Process> processList;
    private ConcurrentMap<DbcData, TableItem> serversTiMap;
    private ConcurrentMap<DbcData, List<Process>> historyMap;
    private List<Process> historyProcessList;
    private Process selectedProcess;
    private List<Process> expandedProcesses;
    private Label appVersionLabel;
    private ToolItem addDbTi;
    private ToolItem removeDbTi;
    private ToolItem editDbTi;
    private ToolItem connectDbTi;
    private ToolItem disconnectDbTi;
    private ToolItem updateTi;
    private ToolItem autoUpdateTi;
    private ToolItem onlyBlockedTi;
    private ToolItem saveBlocksHistory;
    private ToolItem openBlocksHistory;
    private ToolItem terminateProc;
    private ToolItem cancelProc;
    private Text procText;
    private Spinner timerSpinner;
    private SashForm caTreeSf;
    private TableViewer caServersTable;
    private MenuItem connectDbCm;
    private MenuItem disconnectDbCm;
    private MenuItem editDbCm;
    private MenuItem removeDbCm;
    private TreeViewer caMainTree;
    private Composite procComposite;
    private TableViewer bhServersTable;
    private TreeViewer bhMainTree;
    private Composite logComposite;

    private AddDbcDataDlg addDbcDlg;
    private AddDbcDataDlg editDbcDlg;
    
    private Menu caServerListContextMenu;
    //private int timerInterval = TEN;
    private int timerInterval = 3;
    private boolean autoUpdateMode = true;
  ///  private boolean onlyBlocked = false;
    
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
    
    public Process getProcessTree(DbcData dbcData) {
        ProcessTree processTree = processTreeMap.get(dbcData);
        if(processTree == null){
            processTree = new ProcessTree(dbcData);
            processTreeMap.put(dbcData, processTree);
        }
        
        return processTree.getProcessTree();
    }
    
    
    
    
    
    private static final Logger LOG = Logger.getLogger(MainForm.class);
    
    private static Display display;
    private static Shell shell;

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
      //  display.timerExec(1000, timer);
        
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
                        
                       /* caServersTable = new Table(currentActivitySf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
                        {
                            caServersTable.setHeaderVisible(true);
                            caServersTable.setLinesVisible(true);
                            caServersTable.setLayoutData(gridData);
                            TableColumn serversTc = new TableColumn(caServersTable, SWT.NONE);
                            serversTc.setText("Сервер");
                            serversTc.setWidth(200);
                        }*/
                        
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
                            caServersTable.setInput(dbcDataList);
                            
                            caServersTable.refresh();
                           /* TableViewerColumn tvColumn = new TableViewerColumn(caServersTable, SWT.NONE);
                            tvColumn.getColumn().setText("Сервер");*/
                            
                            /*TableColumn serversTc = new TableColumn(caServersTable, SWT.NONE);
                            serversTc.setText("Сервер");
                            serversTc.setWidth(200);*/
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
                                
                                cancelProc = new ToolItem(pcToolBar, SWT.PUSH);
                                cancelProc.setText("Послать сигнал отмены процесса");
                                
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
                            serversTc.getColumn().setText("Блокировка");
                            serversTc.getColumn().setWidth(200);
                            
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
            
            Composite statusBar = new Composite(composite, SWT.NONE);
            {
                statusBar.setLayout(gridLayout);
                statusBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
                appVersionLabel = new Label(statusBar, SWT.HORIZONTAL);
                appVersionLabel.setText("PgSqlBlocks v." + getAppVersion());
            }
            
            
            /*caServerListContextMenu = new Menu(parent, SWT.POP_UP);
            {
                connectDbCm = new MenuItem(caServerListContextMenu, SWT.PUSH);
                connectDbCm.setText("Подключиться");
                disconnectDbCm = new MenuItem(caServerListContextMenu, SWT.PUSH);
                disconnectDbCm.setText("Отключиться");
                editDbCm = new MenuItem(caServerListContextMenu, SWT.PUSH);
                editDbCm.setText("Редактировать");
                removeDbCm = new MenuItem(caServerListContextMenu, SWT.PUSH);
                removeDbCm.setText("Удалить");
            }*/
        }
        
        
        /* getShell().setText(APP_NAME);
        getShell().setMaximized(true);

        GridLayout gridLayout = new GridLayout();
        getShell().setLayout(gridLayout);

        gridLayout.marginHeight = ZERO_MARGIN;
        gridLayout.marginWidth = ZERO_MARGIN;*/



        //GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);


        // Create the tree viewer to display the file tree
        /*treeViewer = new TreeViewer(shell, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
            treeViewer.getTree().setHeaderVisible(true);
            treeViewer.getTree().setLinesVisible(true);
            treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
            for(int i=0;i<treeColsName.length;i++) {
                TreeViewerColumn treeColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
                treeColumn.getColumn().setText(treeColsName[i]);
                treeColumn.getColumn().setWidth(treeColsWidth[i]);
            }
            treeViewer.setContentProvider(new ProcessTreeContentProvider());
            treeViewer.setLabelProvider(new ProcessTreeLabelProvider());
            treeViewer.setInput(new ProcessTreeList(new ConcurrentHashMap<>()));*/
        
        
        
        ////////////////////////////////
        /////     ADD HANDLERS     /////
        ////////////////////////////////
        
        /*caServersTable.addDoubleClickListener(new IDoubleClickListener()
        {
                    @Override
                    public void doubleClick(DoubleClickEvent event) {
                        MessageDialog.openWarning(shell, "222222", "22222222");
                    }

        });*/

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
                    deleteDB.setEnabled(true);
                    editDB.setEnabled(true);
                    connectDB.setEnabled(true);

                    viewProcessTree = getProcessTree(selectedDbcData);
                    // if (selectedDbcData != null && selectedDbcData.isConnected()) {
                    if (selectedDbcData != null) {
                        caMainTree.setInput(viewProcessTree);
                    }


                    caMainTree.refresh();
                    caServersTable.refresh();
                }
            }
        });
        
        
        return parent;

    }

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
    
    protected ToolBarManager createToolBarManager(int style) {
        // Create the toolbar manager
        toolBarManager = new ToolBarManager(style);

        // Add the file actions
        addDb = new Action(Images.ADD_DATABASE.getDescription()) {
            @Override
            public void run() {
                addDbcDlg.open();
                caServersTable.refresh();
            }
        };

        addDb.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.ADD_DATABASE)));

        toolBarManager.add(addDb);

        deleteDB = new Action(Images.DELETE_DATABASE.getDescription()) {
            @Override
            public void run() {
                boolean okPress = MessageDialog.openQuestion(shell, "Подтверждение действия", "Удалить?");
                if (okPress) {
                    dbcDataList.delete(selectedDbcData);
                }
                caServersTable.refresh();
            }
        };

        deleteDB.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.DELETE_DATABASE)));
        deleteDB.setEnabled(false);

        toolBarManager.add(deleteDB);

        editDB = new Action(Images.EDIT_DATABASE.getDescription()) {
            @Override
            public void run() {
                editDbcDlg = new AddDbcDataDlg(shell, selectedDbcData);
                editDbcDlg.open();
                caServersTable.refresh();
            }
        };

        editDB.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.EDIT_DATABASE)));
        editDB.setEnabled(false);

        toolBarManager.add(editDB);

        // Add a separator
        toolBarManager.add(new Separator());

        connectDB = new Action(Images.CONNECT_DATABASE.getDescription()) {
            @Override
            public void run() {
               selectedDbcData.connect();
               
               deleteDB.setEnabled(false);
               editDB.setEnabled(false);
               connectDB.setEnabled(false);
               disconnectDB.setEnabled(true);
               
               caMainTree.setInput(null);
               caMainTree.setInput(viewProcessTree);
               caMainTree.refresh();
               caServersTable.refresh();
            }
        };

        connectDB.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.CONNECT_DATABASE)));
        connectDB.setEnabled(false);

        toolBarManager.add(connectDB);

        disconnectDB = new Action(Images.DISCONNECT_DATABASE.getDescription()) {
            @Override
            public void run() {
                selectedDbcData.disconnect();
                
                deleteDB.setEnabled(true);
                editDB.setEnabled(true);
                connectDB.setEnabled(true);
                disconnectDB.setEnabled(false);
                
                caMainTree.setInput(null);
                caMainTree.setInput(viewProcessTree);
                caMainTree.refresh();
                caServersTable.refresh();
            }
        };

        disconnectDB.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.DISCONNECT_DATABASE)));
        disconnectDB.setEnabled(false);

        toolBarManager.add(disconnectDB);

        // Add a separator
        toolBarManager.add(new Separator());

        update = new Action(Images.UPDATE.getDescription()) {
            @Override
            public void run() {
                caMainTree.setInput(null);
                caMainTree.setInput(viewProcessTree);
                caMainTree.refresh();
                caServersTable.refresh();
            }
        };

        update.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.UPDATE)));

        toolBarManager.add(update);

        autoUpdate = new Action(Images.AUTOUPDATE.getDescription()) {
            @Override
            public void run() {
                setStatus("Hello world1");
            }
        };

        autoUpdate.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.AUTOUPDATE)));
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

        onlyBlocked = new Action(Images.VIEW_ONLY_BLOCKED.getDescription()) {
            @Override
            public void run() {
                setStatus("Hello world1");
            }
        };

        onlyBlocked.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.VIEW_ONLY_BLOCKED)));

        toolBarManager.add(onlyBlocked);

        exportBlocks = new Action(Images.EXPORT_BLOCKS.getDescription()) {
            @Override
            public void run() {
               // BlocksHistory.getInstance().save();
                LOG.info("Блокировка сохранена...");
            }
        };

        exportBlocks.setImageDescriptor(ImageDescriptor.createFromImage(getImage(Images.EXPORT_BLOCKS)));

        toolBarManager.add(exportBlocks);

        importBlocks = new Action(Images.IMPORT_BLOCKS.getDescription()) {
            @Override
            public void run() {
                FileDialog fd = new FileDialog(shell);
                fd.setFilterPath(System.getProperty("user.home") + "/BlocksHistory");
                fd.setText("Открыть историю блокировок");
                fd.setFilterExtensions(new String[]{"*.xml"});
               // BlocksHistory.getInstance().open(fd.open());
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
            if(autoUpdateMode){
                viewProcessTree = getProcessTree(selectedDbcData);
                caMainTree.setInput(viewProcessTree);
                display.timerExec(timerInterval * 1000, this);
            }
        }
    };
    
    private void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdateMode = true;
        display.timerExec(1000, timer);
    }
    
}

