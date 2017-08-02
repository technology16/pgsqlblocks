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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DBProcessInfoView extends Composite {

    private Settings settings = Settings.getInstance();
    private ResourceBundle resourceBundle = settings.getResourceBundle();

    private final List<DBProcessInfoViewListener> listeners = new ArrayList<>();

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
        toolBar.setEnabled(false);
        GridLayout layout = new GridLayout();
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        toolBar.setLayout(layout);
        toolBar.setLayoutData(layoutData);

        cancelProcessToolItem = new ToolItem(toolBar, SWT.PUSH);
        cancelProcessToolItem.setText(resourceBundle.getString("cancel_process"));
        cancelProcessToolItem.addListener(SWT.Selection, event -> {
            listeners.forEach(DBProcessInfoViewListener::dbProcessInfoViewCancelProcessToolItemClicked);
        });

        terminateProcessToolItem = new ToolItem(toolBar, SWT.PUSH);
        terminateProcessToolItem.setText(resourceBundle.getString("kill_process"));
        terminateProcessToolItem.addListener(SWT.Selection, event -> {
            listeners.forEach(DBProcessInfoViewListener::dbProcessInfoViewTerminateProcessToolItemClicked);
        });

        processInfoText = new Text(this, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        GridData textLayoutData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        textLayoutData.heightHint = 200;
        processInfoText.setLayoutData(textLayoutData);
    }

    public void hideToolBar() {
        this.toolBar.setVisible(false);
        GridData layoutData = (GridData) this.toolBar.getLayoutData();
        layoutData.exclude = true;
        this.layout();
    }

    public void showToolBar() {
        this.toolBar.setVisible(true);
        GridData layoutData = (GridData) this.toolBar.getLayoutData();
        layoutData.exclude = false;
        this.layout();
    }

    public void setTextContent(String content) {
        processInfoText.setText(content);
    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    public void show(DBProcess process) {
        StringBuilder stringBuilder = new StringBuilder();
        DBProcessesViewDataSource dataSource = new DBProcessesViewDataSource(resourceBundle, null);
        for (int i = 0; i< dataSource.numberOfColumns(); i++) {
            String title = dataSource.columnTitleForColumnIndex(i);
            String content = dataSource.getColumnText(process, i);
            stringBuilder.append(title);
            stringBuilder.append(": ");
            stringBuilder.append(content);
            stringBuilder.append("\n");
        }

        processInfoText.setText(stringBuilder.toString());
        this.setVisible(true);
        GridData layoutData = (GridData) this.getLayoutData();
        layoutData.exclude = false;
        this.getParent().layout();
    }

    public void hide() {
        processInfoText.setText("");
        this.setVisible(false);
        GridData layoutData = (GridData) this.getLayoutData();
        layoutData.exclude = true;
        this.getParent().layout();
    }

    public void addListener(DBProcessInfoViewListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBProcessInfoViewListener listener) {
        listeners.remove(listener);
    }

}
