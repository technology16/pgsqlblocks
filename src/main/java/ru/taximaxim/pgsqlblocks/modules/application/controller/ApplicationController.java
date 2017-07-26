package ru.taximaxim.pgsqlblocks.modules.application.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabItem;
import ru.taximaxim.pgsqlblocks.common.DBModelsLocalProvider;
import ru.taximaxim.pgsqlblocks.modules.application.view.ApplicationView;
import ru.taximaxim.pgsqlblocks.modules.application.view.ApplicationViewListener;
import ru.taximaxim.pgsqlblocks.modules.logs.view.LogsView;
import ru.taximaxim.pgsqlblocks.modules.processes.controller.ProcessesController;
import ru.taximaxim.pgsqlblocks.modules.processes.view.ProcessesView;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.util.ResourceBundle;

public class ApplicationController implements ApplicationViewListener {

    private final ApplicationView applicationView;

    private final Settings settings = Settings.getInstance();
    private final ResourceBundle resourceBundle = settings.getResourceBundle();

    private LogsView logsView;
    // submodules
    private ProcessesController processesController;

    public ApplicationController() {
        applicationView = new ApplicationView();
        applicationView.setListener(this);
    }

    public void launchWithArgs(String[] args) {
        applicationView.show();
    }

    @Override
    public void applicationViewDidLoad() {

        logsView = new LogsView(applicationView.getBottomPanelComposite(), SWT.NONE);

        processesController = new ProcessesController(new DBModelsLocalProvider());
        TabItem processesTabItem = new TabItem(applicationView.getTabFolder(), SWT.NONE);
        processesTabItem.setText(resourceBundle.getString("current_activity"));
        ProcessesView processesView = new ProcessesView(applicationView.getTabFolder(), SWT.NONE);
        processesTabItem.setControl(processesView);
        processesController.setView(processesView);
        processesController.load();
    }

    public void hideLogsView() {
        applicationView.hideBottomPanel();
    }

    public void showLogsView() {
        applicationView.showBottomPanel();
    }

    @Override
    public void applicationViewWillDisappear() {
        processesController.close();
    }


}
