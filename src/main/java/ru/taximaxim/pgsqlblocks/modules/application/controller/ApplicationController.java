/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
 * %
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.modules.application.controller;

import org.eclipse.swt.SWT;

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
        new LogsView(applicationView.getBottomPanelComposite(), SWT.NONE);
        processesController = new ProcessesController(settings);
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
