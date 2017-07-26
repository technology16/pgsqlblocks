package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class DBProcessInfoView extends Composite {

    private Settings settings = Settings.getInstance();
    private ResourceBundle resourceBundle = settings.getResourceBundle();

    private ToolBar toolBar;
    private ToolItem cancelProcessToolItem;
    private ToolItem terminateProcessToolItem;
    private Text processInfoText;

    public DBProcessInfoView(Composite parent, int style) {
        super(parent, style);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
        toolBar = new ToolBar(this, SWT.HORIZONTAL);
        GridLayout layout = new GridLayout();
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        toolBar.setLayout(layout);
        toolBar.setLayoutData(layoutData);

        cancelProcessToolItem = new ToolItem(toolBar, SWT.PUSH);
        cancelProcessToolItem.setText("Cancel process");

        terminateProcessToolItem = new ToolItem(toolBar, SWT.PUSH);
        terminateProcessToolItem.setText("Terminate process");

        processInfoText = new Text(this, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        GridData textLayoutData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        textLayoutData.heightHint = 150;
        processInfoText.setLayoutData(textLayoutData);
    }

    public void setTextContent(String content) {
        processInfoText.setText(content);
    }

    public void show(DBProcess process) {
        String pidTitle = resourceBundle.getString("pid");
        String backendStartTitle = resourceBundle.getString("backend_start");
        String queryStartTitle = resourceBundle.getString("query_start");
        String xactStartTitle = resourceBundle.getString("xact_start");
        String slowQueryTitle = resourceBundle.getString("slow_query");
        String queryTitle = resourceBundle.getString("query");

        String pid = String.valueOf(process.getPid());
        String isSlowQuery = String.valueOf(process.getQuery().isSlowQuery());
        String backendStart = DateUtils.dateToString(process.getQuery().getBackendStart());
        String queryStart = DateUtils.dateToString(process.getQuery().getQueryStart());
        String xactStart = DateUtils.dateToString(process.getQuery().getXactStart());
        String query = process.getQuery().getQueryString();

        String content = MessageFormat.format("{0}: {1}\n{2} :{3}\n{4}: {5}\n{6}: {7}\n{8}: {9}\n{10}: {11}",
                pidTitle, pid,backendStartTitle, backendStart, queryStartTitle, queryStart, xactStartTitle, xactStart,
                slowQueryTitle, isSlowQuery, queryTitle, query);

        processInfoText.setText(content);
        this.setVisible(true);
        GridData layoutData = (GridData) this.getLayoutData();
        layoutData.exclude = false;
        this.getParent().layout();
    }

    public void hide() {
        this.setVisible(false);
        GridData layoutData = (GridData) this.getLayoutData();
        layoutData.exclude = true;
        this.getParent().layout();
    }

}
