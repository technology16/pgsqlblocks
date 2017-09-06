/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DBProcessInfoView extends Composite {

    private final ResourceBundle resourceBundle;

    private final List<DBProcessInfoViewListener> listeners = new ArrayList<>();

    private ToolBar toolBar;
    private Text processInfoText;

    public DBProcessInfoView(ResourceBundle resourceBundle, Composite parent, int style) {
        super(parent, style);
        this.resourceBundle = resourceBundle;
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

        ToolItem cancelProcessToolItem = new ToolItem(toolBar, SWT.PUSH);
        cancelProcessToolItem.setText(resourceBundle.getString("cancel_process"));
        cancelProcessToolItem.addListener(SWT.Selection, event -> {
            listeners.forEach(DBProcessInfoViewListener::dbProcessInfoViewCancelProcessToolItemClicked);
        });

        ToolItem terminateProcessToolItem = new ToolItem(toolBar, SWT.PUSH);
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

    public ToolBar getToolBar() {
        return toolBar;
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
