/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.modules.application.view;

import static ru.taximaxim.pgsqlblocks.PgSqlBlocks.APP_NAME;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import ru.taximaxim.pgsqlblocks.ui.AboutDlg;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;
import ru.taximaxim.pgsqlblocks.utils.Images;
import ru.taximaxim.pgsqlblocks.utils.Settings;

public class ApplicationView extends ApplicationWindow {

    private static final Logger LOG = LogManager.getLogger(ApplicationView.class);

    private Composite topPanelComposite;
    private Composite bottomPanelComposite;

    private SashForm sashForm;

    private ToolBarManager toolBarManager;

    private Display display;

    private Tray tray;

    private ApplicationViewListener viewListener;

    private static final int[] ICON_SIZES = { 32, 48, 256/*, 512*/ };

    private final Settings settings;
    private final ResourceBundle resourceBundle;

    public ApplicationView(Settings settings) {
        super(null);
        this.settings = settings;
        this.resourceBundle = settings.getResourceBundle();
        setBlockOnOpen(true);
        addToolBar(SWT.RIGHT | SWT.FLAT);
    }

    public void show() {
        try {
            display = Display.getDefault();
            open();
            display.dispose();
        } catch (Exception exception) {
            LOG.error("An error has occurred:" + exception);
        }
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
            icons[i] = new Image(null, getClass().getClassLoader().getResourceAsStream(
                    MessageFormat.format("images/block-{0}x{0}.png", ICON_SIZES[i])));
        }
        return icons;
    }

    @Override
    protected ToolBarManager createToolBarManager(int style) {
        toolBarManager = new ToolBarManager();
        return toolBarManager;
    }

    @Override
    public ToolBarManager getToolBarManager() {
        return toolBarManager;
    }

    @Override
    protected Control createContents(Composite parent) {
        initTray();

        createApplicationMenu();

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashForm = new SashForm(parent, SWT.VERTICAL);
        sashForm.setLayout(layout);
        sashForm.setLayoutData(layoutData);
        sashForm.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        createTopPanel(sashForm);

        createBottomPanel(sashForm);

        sashForm.setSashWidth(2);
        sashForm.setWeights(new int[] {80,20});

        if (viewListener != null) {
            viewListener.applicationViewDidLoad();
        }

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

    private void initTray() {
        tray = display.getSystemTray();
        if (tray != null) {
            TrayItem trayItem = new TrayItem(tray, SWT.NONE);
            trayItem.setImage(ImageUtils.getImage(Images.UNBLOCKED));
            trayItem.setToolTipText(APP_NAME);
            final Menu trayMenu = new Menu(getShell(), SWT.POP_UP);
            MenuItem trayMenuItem = new MenuItem(trayMenu, SWT.PUSH);
            trayMenuItem.setText(resourceBundle.getString("exit"));
            trayMenuItem.addListener(SWT.Selection, event -> {
                if (settings.isConfirmExit()) {
                    getShell().forceActive();
                }
                getShell().close();
            });
            trayItem.addListener(SWT.MenuDetect, event -> trayMenu.setVisible(true));

            ToolTip tip = new ToolTip(getShell(), SWT.BALLOON | SWT.ICON_WARNING);
            tip.setText(APP_NAME);
            tip.setAutoHide(true);
            tip.setVisible(false);
            trayItem.setToolTip(tip);
        }
    }

    private void createTopPanel(SashForm sashForm) {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        topPanelComposite = new Composite(sashForm, SWT.NONE);
        topPanelComposite.setLayout(layout);
        topPanelComposite.setLayoutData(layoutData);
    }

    private void createBottomPanel(SashForm sashForm) {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        bottomPanelComposite = new Composite(sashForm, SWT.NONE);
        bottomPanelComposite.setLayout(layout);
        bottomPanelComposite.setLayoutData(layoutData);
    }

    public void hideBottomPanel() {
        sashForm.setWeights(new int[] {100, 0});
    }

    public void showBottomPanel() {
        sashForm.setWeights(new int[] {80,20});
    }

    public Composite getTopPanelComposite() {
        return topPanelComposite;
    }

    public Composite getBottomPanelComposite() {
        return bottomPanelComposite;
    }

    public Tray getTray() {
        return tray;
    }

    public boolean trayIsAvailable() {
        return tray != null;
    }

    public void setListener(ApplicationViewListener listener) {
        viewListener = listener;
    }

    @Override
    protected boolean canHandleShellCloseEvent() {
        if (settings.isConfirmExit() && !MessageDialog.openQuestion(getShell(), resourceBundle.getString("confirm_action"),
                resourceBundle.getString("exit_confirm_message"))) {
            return false;
        }
        if (viewListener != null) {
            viewListener.applicationViewWillDisappear();
        }
        return super.canHandleShellCloseEvent();
    }
}
