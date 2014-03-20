package ru.taximaxim.psSqlBlocks.ui;


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

public class MainForm {

    private static final String APP_NAME = "pgSqlBlocks";
    private static final int ZERO_MARGIN = 0;
    private static final int[] VERTICAL_WEIGHTS = new int[]{70,30};
    private static final int[] HORIZONTAL_WEIGHTS = new int[]{20,80};
    private static final int SASH_WIDTH = 5;
    private static MainForm mainForm;
    private Shell shell;
    private MainForm() {}

    Menu menu;
    MenuItem serversMi;
    Menu serversMenu;
    MenuItem registerMi;
    MenuItem processesMi;
    Menu processesMenu;
    MenuItem viewBlocksMi;
    MenuItem infoMi;
    Menu infoMenu;
    MenuItem aboutMi;
    ToolBar toolBar;

    public static MainForm getInstance() {
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

        SashForm verticalSash = new SashForm(shell, SWT.VERTICAL);
        {
            verticalSash.setLayout(gridLayout);
            verticalSash.setLayoutData(gridData);
            verticalSash.SASH_WIDTH = SASH_WIDTH;
            verticalSash.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

            Composite topComposite = new Composite(verticalSash, SWT.NONE);
            topComposite.setLayout(gridLayout);

            SashForm horizontalSash = new SashForm(topComposite, SWT.HORIZONTAL);
            {
                horizontalSash.setLayout(gridLayout);
                horizontalSash.setLayoutData(gridData);
                horizontalSash.SASH_WIDTH = SASH_WIDTH;
                horizontalSash.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

                Composite serverComposite = new Composite(horizontalSash, SWT.NONE);
                {
                    serverComposite.setLayout(gridLayout);
                    TabFolder serversTf = new TabFolder(serverComposite, SWT.BORDER);
                    {
                        serversTf.setLayoutData(gridData);
                        TabItem currentActivityTi = new TabItem(serversTf, SWT.NONE);
                        {
                            currentActivityTi.setText("Текущая активность");
                            Table currentActivityTable = new Table(serversTf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                            {
                                currentActivityTable.setHeaderVisible(true);
                                currentActivityTable.setLinesVisible(true);
                                currentActivityTable.setLayoutData(gridData);
                                currentActivityTi.setControl(currentActivityTable);
                            }
                        }

                        TabItem blocksHistoryTi = new TabItem(serversTf, SWT.NONE);
                        {
                            blocksHistoryTi.setText("История блокировок");
                            Table blocksHistoryTable = new Table(serversTf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
                            {
                                blocksHistoryTable.setHeaderVisible(true);
                                blocksHistoryTable.setLinesVisible(true);
                                blocksHistoryTable.setLayoutData(gridData);
                                blocksHistoryTi.setControl(blocksHistoryTable);
                            }
                        }
                    }
                }

                Composite tableComposite = new Composite(horizontalSash, SWT.NONE);
                {
                    tableComposite.setLayout(gridLayout);
                    Table mainTable = new Table(tableComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
                    {
                        mainTable.setLayoutData(gridData);
                        mainTable.setHeaderVisible(true);
                        mainTable.setLinesVisible(true);
                    }
                }

                horizontalSash.setWeights(HORIZONTAL_WEIGHTS);
            }

            Composite logComposite = new Composite(verticalSash, SWT.NONE);
            {
                logComposite.setLayout(gridLayout);
                Table logTable = new Table(logComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                {
                    logTable.setLayoutData(gridData);
                    logTable.setHeaderVisible(true);
                    logTable.setLinesVisible(true);
                }
            }

            verticalSash.setWeights(VERTICAL_WEIGHTS);
        }

        Composite statusBar = new Composite(shell, SWT.BORDER);
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
