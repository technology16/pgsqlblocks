package ru.taximaxim.pgsqlblocks.modules.application.controller;

import org.eclipse.swt.SWT;
import ru.taximaxim.pgsqlblocks.common.DBModelsLocalProvider;
import ru.taximaxim.pgsqlblocks.modules.application.view.ApplicationView;
import ru.taximaxim.pgsqlblocks.modules.application.view.ApplicationViewListener;
import ru.taximaxim.pgsqlblocks.modules.logs.view.LogsView;
import ru.taximaxim.pgsqlblocks.modules.processes.controller.ProcessesController;
import ru.taximaxim.pgsqlblocks.modules.processes.view.ProcessesView;
import ru.taximaxim.pgsqlblocks.utils.Settings;

public class ApplicationController implements ApplicationViewListener {

    private final ApplicationView applicationView;

    private final Settings settings = Settings.getInstance();

    private ProcessesController processesController;

    public ApplicationController() {
        applicationView = new ApplicationView(settings);
        applicationView.setListener(this);
    }

    public void launch() {
        applicationView.show();
    }

    @Override
    public void applicationViewDidLoad() {
        new LogsView(applicationView.getBottomPanelComposite(), settings, SWT.NONE);
        processesController = new ProcessesController(settings, new DBModelsLocalProvider());
        ProcessesView processesView = new ProcessesView(applicationView.getTopPanelComposite(), SWT.NONE);
        processesController.setView(processesView);
        processesController.load();
    }

    public ApplicationView getApplicationView() {
        return applicationView;
    }

    @Override
    public void applicationViewWillDisappear() {
        processesController.close();
    }
}
