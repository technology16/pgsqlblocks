/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2018 "Technology" LLC
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
package ru.taximaxim.pgsqlblocks.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;

import java.util.ResourceBundle;

/**
 * Dialog for current process info
 */
public class DBProcessInfoDialog extends Dialog{

    private ResourceBundle resourceBundle;
    private DBProcess dbProcess;
    private DBBlocksJournalProcess dbBlocksProcess;
    private ProcessInfoListener processInfoListener;
    private boolean disabledButton;
    private static final int TEXT_WIDTH = 200;
    private final DateUtils dateUtils = new DateUtils();

    public DBProcessInfoDialog(ResourceBundle resourceBundle, Shell parentShell, Object process, boolean disabledButton) {
        super(parentShell);
        this.dbProcess = process instanceof DBProcess ? ((DBProcess) process) : null;
        this.dbBlocksProcess = process instanceof DBBlocksJournalProcess ? ((DBBlocksJournalProcess) process) : null;
        this.resourceBundle = resourceBundle;
        this.processInfoListener = null;
        this.disabledButton = disabledButton;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(resourceBundle.getString("process_info"));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        //Need to override to hide OK/Cancel button
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        layout.marginTop = 10;
        container.setLayout(layout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        container.setLayoutData(gridData);

        GridData textGd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        textGd.widthHint = TEXT_WIDTH;
        if (dbBlocksProcess != null) {
            dbProcess = dbBlocksProcess.getProcess();
        }
        createProcessArea(container, textGd, "pid", String.valueOf(dbProcess.getPid()));
        createProcessArea(container, textGd, "num_of_blocked_processes", String.valueOf(dbProcess.getChildren().size()));
        createProcessArea(container, textGd, "db_name", dbProcess.getQueryCaller().getDatabaseName());
        createProcessArea(container, textGd, "application", dbProcess.getQueryCaller().getApplicationName());
        createProcessArea(container, textGd, "user_name", dbProcess.getQueryCaller().getUserName());
        createProcessArea(container, textGd, "query_start", dateUtils.dateToString(dbProcess.getQuery().getQueryStart()));
        createProcessArea(container, textGd, "xact_start", dateUtils.dateToString(dbProcess.getQuery().getXactStart()));
        createProcessArea(container, textGd, "duration", dbProcess.getQuery().getDuration());
        createProcessArea(container, textGd, "state", dbProcess.getState());
        createQueryArea(container, dbProcess.getQuery().getQueryString());
        createButtonArea(container);
        return container;
    }

    private void createButtonArea(Composite container) {
        Button cancelButton = new Button(container, SWT.PUSH);
        cancelButton.setText(resourceBundle.getString("cancel_process"));
        cancelButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (processInfoListener != null) {
                    processInfoListener.cancelButtonClick();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Button terminateButton = new Button(container, SWT.PUSH);
        terminateButton.setText(resourceBundle.getString("kill_process"));
        terminateButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (processInfoListener != null) {
                    processInfoListener.terminateButtonClick();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        if (disabledButton) {
            cancelButton.setEnabled(false);
            terminateButton.setEnabled(false);
        }
    }

    private void createProcessArea(Composite container, GridData gridData, String type, String data) {
        Label pidLabel = new Label(container, SWT.HORIZONTAL);
        pidLabel.setText(resourceBundle.getString(type));
        Text pid = new Text(container,  SWT.FILL);
        pid.setEditable(false);
        if (data != null) {
            pid.setText(data);
        }else {
            pid.setText("");
        }
        pid.setLayoutData(gridData);
    }

    private void createQueryArea(Composite container, String data) {
        Composite composite = new Composite(container, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 20));
        Label pidLabel = new Label(composite, SWT.HORIZONTAL);
        pidLabel.setText(resourceBundle.getString("query"));

        StyledText pid = new StyledText(composite,   SWT.MULTI |SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.READ_ONLY);

        if (data != null) {
            pid.setText(data);
        }else {
            pid.setText("");
        }
        pid.setWordWrap(true);
        GridData grid = new GridData(SWT.FILL, SWT.FILL, true, true);
        grid.widthHint = 500;
        pid.setLayoutData(grid);
    }

    public void setProcessInfoListener(ProcessInfoListener processInfoListener) {
        this.processInfoListener = processInfoListener;
    }

    public interface ProcessInfoListener {
        void terminateButtonClick();
        void cancelButtonClick();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }
}
