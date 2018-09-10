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
package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DBProcessInfoView extends Composite {

    private final ResourceBundle resourceBundle;

    private final List<DBProcessInfoViewListener> listeners = new ArrayList<>();

    private Composite buttonBar;
    private Text processInfoText;
    private GridData layoutData;

    public DBProcessInfoView(ResourceBundle resourceBundle, Composite parent, int style) {
        super(parent, style);
        this.resourceBundle = resourceBundle;
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layoutData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
        buttonBar = new Composite(this, SWT.NULL);
        GridLayout layout = new GridLayout(2, false);
        GridData btnData = new GridData(SWT.FILL, SWT.TOP, true, false);
        buttonBar.setLayout(layout);
        buttonBar.setLayoutData(btnData);

        Button cancelProcessButton = new Button(buttonBar, SWT.PUSH);
        cancelProcessButton.setText(resourceBundle.getString("cancel_process"));
        cancelProcessButton.setToolTipText("pg_cancel_backend");
        cancelProcessButton.addListener(SWT.Selection, event -> {
            listeners.forEach(DBProcessInfoViewListener::dbProcessInfoViewCancelProcessButtonClicked);
        });

        Button terminateProcessButton = new Button(buttonBar, SWT.PUSH);
        terminateProcessButton.setText(resourceBundle.getString("kill_process"));
        terminateProcessButton.setToolTipText("pg_terminate_backend");
        terminateProcessButton.addListener(SWT.Selection, event -> {
            listeners.forEach(DBProcessInfoViewListener::dbProcessInfoViewTerminateProcessButtonClicked);
        });

        processInfoText = new Text(this, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textLayoutData.heightHint = 200;
        processInfoText.setLayoutData(textLayoutData);
    }

    public void hideToolBar() {
        this.buttonBar.setVisible(false);
        GridData layoutData = (GridData) this.buttonBar.getLayoutData();
        layoutData.exclude = true;
        this.layout();
    }

    public void show(DBProcess process) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s = %d\n",
                resourceBundle.getString("pid"), process.getPid()));
        stringBuilder.append(String.format("%s = %s\n",
                resourceBundle.getString("user_name"), process.getQueryCaller().getUserName()));
        stringBuilder.append(String.format("%s = %s\n",
                resourceBundle.getString("db_name"), process.getQueryCaller().getDatabaseName()));
        stringBuilder.append(String.format("\n%s:\n%s\n",
                resourceBundle.getString("query"), process.getQuery().getQueryString()));

        processInfoText.setText(stringBuilder.toString());
        this.setVisible(true);
        GridData layoutData = this.layoutData;
        layoutData.exclude = false;
        this.getParent().layout();
    }

    public void hide() {
        processInfoText.setText("");
        this.setVisible(false);
        GridData layoutData = this.layoutData;
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
