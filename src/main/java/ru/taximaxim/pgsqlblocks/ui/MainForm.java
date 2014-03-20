package ru.taximaxim.pgsqlblocks.ui;


import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public final class MainForm {

    private static final String APP_NAME = "pgSqlBlocks";
    private static final int ZERO_MARGIN = 0;
    private static final int[] VERTICAL_WEIGHTS = new int[]{70,30};
    private static final int[] HORIZONTAL_WEIGHTS = new int[]{20,80};
    private static final int SASH_WIDTH = 5;
    private static Logger log = Logger.getLogger(MainForm.class);
    private static MainForm mainForm;
    private Shell shell;
    private MainForm() {}

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

    private Composite topComposite;
    private TabFolder tabPanel;
    private TabItem currentActivityTi;
    private SashForm currentActivitySh;
    private Table caServersTable;
    private Table caMainTable;
    private TabItem blocksHistoryTi;
    private SashForm blocksHistorySh;
    private Table bhServersTable;
    private Table bhMainTable;
    
    private SashForm verticalSash;
    private Composite logComposite;
    private Table logTable;
    private Composite statusBar;

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
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch(Exception e){
            log.error(e);
        }
        finally {
            shell = null;
        }
    }

    private void createControls() {
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

        toolBar = new ToolBar(shell, SWT.RIGHT);
        {
            ToolItem examplePush = new ToolItem(toolBar, SWT.PUSH);
            examplePush.setText("PUSH");
            ToolItem exampleCheck = new ToolItem(toolBar, SWT.CHECK);
            exampleCheck.setText("CHECK");
        }

        verticalSash = new SashForm(shell, SWT.VERTICAL);
        {
            verticalSash.setLayout(gridLayout);
            verticalSash.setLayoutData(gridData);
            verticalSash.SASH_WIDTH = SASH_WIDTH;
            verticalSash.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

            topComposite = new Composite(verticalSash, SWT.NONE);
            topComposite.setLayout(gridLayout);

            tabPanel = new TabFolder(topComposite, SWT.BORDER);
            {
                tabPanel.setLayoutData(gridData);
                currentActivityTi = new TabItem(tabPanel, SWT.NONE);
                {
                    currentActivityTi.setText("Текущая активность");
                    currentActivitySh = new SashForm(tabPanel, SWT.HORIZONTAL);
                    {
                        currentActivitySh.setLayout(gridLayout);
                        currentActivitySh.setLayoutData(gridData);
                        currentActivitySh.SASH_WIDTH = SASH_WIDTH;
                        currentActivitySh.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                        
                        caServersTable = new Table(currentActivitySh, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                        {
                            caServersTable.setHeaderVisible(true);
                            caServersTable.setLinesVisible(true);
                            caServersTable.setLayoutData(gridData);
                        }
                        caMainTable = new Table(currentActivitySh, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                        {
                            caMainTable.setHeaderVisible(true);
                            caMainTable.setLinesVisible(true);
                            caMainTable.setLayoutData(gridData);
                        }
                    }
                    currentActivitySh.setWeights(HORIZONTAL_WEIGHTS);
                    currentActivityTi.setControl(currentActivitySh);
                }
                blocksHistoryTi = new TabItem(tabPanel, SWT.NONE);
                {
                    blocksHistoryTi.setText("История блокировок");
                    blocksHistorySh = new SashForm(tabPanel, SWT.HORIZONTAL);
                    {
                        blocksHistorySh.setLayout(gridLayout);
                        blocksHistorySh.setLayoutData(gridData);
                        blocksHistorySh.SASH_WIDTH = SASH_WIDTH;
                        blocksHistorySh.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

                        bhServersTable = new Table(blocksHistorySh, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                        {
                            bhServersTable.setHeaderVisible(true);
                            bhServersTable.setLinesVisible(true);
                            bhServersTable.setLayoutData(gridData);
                        }
                        bhMainTable = new Table(blocksHistorySh, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                        {
                            bhMainTable.setHeaderVisible(true);
                            bhMainTable.setLinesVisible(true);
                            bhMainTable.setLayoutData(gridData);
                        }
                    }
                    blocksHistorySh.setWeights(HORIZONTAL_WEIGHTS);
                    blocksHistoryTi.setControl(blocksHistorySh);
                }
            }
            logComposite = new Composite(verticalSash, SWT.NONE);
            {
                logComposite.setLayout(gridLayout);
                logTable = new Table(logComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                {
                    logTable.setLayoutData(gridData);
                    logTable.setHeaderVisible(true);
                    logTable.setLinesVisible(true);
                }
            }
            verticalSash.setWeights(VERTICAL_WEIGHTS);
        }

        statusBar = new Composite(shell, SWT.BORDER);
        {
            statusBar.setLayout(gridLayout);
            statusBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
            Label statusLabel = new Label(statusBar, SWT.HORIZONTAL);
            statusLabel.setText("statusbar");
        }
    }

    private void addHandlers() {

    }
}
