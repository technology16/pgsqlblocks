package ru.taximaxim.pgsqlblocks.modules.application.view;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class ApplicationView extends ApplicationWindow {

    private Composite composite;
    private Composite topPanelComposite;
    private Composite bottomPanelComposite;

    private SashForm sashForm;

    private ToolBarManager toolBarManager;

    private Display display;

    private ApplicationViewListener viewListener;

    private static final String APP_NAME = "pgSqlBlocks";
    private static final int[] ICON_SIZES = { 32, 48, 256/*, 512*/ };

    private Settings settings = Settings.getInstance();
    private ResourceBundle resourceBundle = settings.getResourceBundle();

    public ApplicationView() {
        super(null);
        setBlockOnOpen(true);
        addToolBar(SWT.RIGHT | SWT.FLAT);
    }

    public void show() {
        try {
            display = Display.getCurrent();
            open();
            if (display != null)
                display.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void constrainShellSize() {
        super.constrainShellSize();
        getShell().setMaximized( true );
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setMinimumSize(1024, 768);
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
    protected ToolBarManager createToolBarManager(int style) {
        toolBarManager = new ToolBarManager();
        return toolBarManager;
    }

    @Override
    public ToolBarManager getToolBarManager() {
        return toolBarManager;
    }

    @Override
    protected Control createContents(Composite mainComposite) {
        this.composite = mainComposite;

        GridLayout layout = new GridLayout();
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashForm = new SashForm(mainComposite, SWT.VERTICAL);
        sashForm.setLayout(layout);
        sashForm.setLayoutData(layoutData);
        sashForm.SASH_WIDTH = 2;
        sashForm.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        createTopPanel(sashForm);

        createBottomPanel(sashForm);

        sashForm.setSashWidth(2);
        sashForm.setWeights(new int[] {80,20});

        if (viewListener != null) {
            viewListener.applicationViewDidLoad();
        }

        return  mainComposite;
    }

    private void createTopPanel(SashForm sashForm) {
        GridLayout layout = new GridLayout();
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

    public void setListener(ApplicationViewListener listener) {
        viewListener = listener;
    }

    @Override
    protected void handleShellCloseEvent() {
        super.handleShellCloseEvent();
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
