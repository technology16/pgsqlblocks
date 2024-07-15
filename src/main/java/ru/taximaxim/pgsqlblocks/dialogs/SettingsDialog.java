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
package ru.taximaxim.pgsqlblocks.dialogs;

import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import ru.taximaxim.pgsqlblocks.utils.ImageUtils;
import ru.taximaxim.pgsqlblocks.utils.Images;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.Settings;

public class SettingsDialog extends Dialog {

    private final Settings settings;
    private final ResourceBundle resourceBundle;

    private Spinner updatePeriod;
    private Spinner limitBlock;
    private Button showIdleButton;
    private Button showBackendPidButton;
    private Button showToolTip;
    private Button confirmRequiredButton;
    private Button confirmExitButton;
    private Combo languageCombo;
    private Text textPath;

    public SettingsDialog(Settings settings, Shell shell) {
        super(shell);
        this.settings = settings;
        this.resourceBundle = settings.getResourceBundle();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        container.setLayout(layout);

        populateGeneralGroup(container);
        populateProcessGroup(container);
        populateNotificationGroup(container);
        populateBlockJournalPathGroup(container);
        return container;
    }

    private void populateGeneralGroup(Composite container) {
        Group generalGroup = createGroup(container, resourceBundle.getString("general"), 2);

        Label selectLocale = new Label(generalGroup, SWT.HORIZONTAL);
        selectLocale.setText(resourceBundle.getString("select_ui_language"));
        selectLocale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        languageCombo = new Combo (generalGroup, SWT.READ_ONLY);
        languageCombo.setItems(Settings.SUPPORTED_LANGUAGES);
        languageCombo.select(languageCombo.indexOf(settings.getLocale().getLanguage()));
    }

    private void populateProcessGroup(Composite container) {
        Group processGroup = createGroup(container, resourceBundle.getString("processes"), 3);

        Label updatePeriodLabel = new Label(processGroup, SWT.HORIZONTAL);
        updatePeriodLabel.setText(resourceBundle.getString("auto_update_interval"));

        GridData textGd = new GridData(GridData.FILL_HORIZONTAL);
        textGd.horizontalSpan = 2;
        textGd.horizontalIndent = 50;
        updatePeriod = new Spinner(processGroup, SWT.BORDER);
        updatePeriod.setLayoutData(textGd);
        updatePeriod.setMinimum(1);
        updatePeriod.setMaximum(100);
        updatePeriod.setSelection(settings.getUpdatePeriodSeconds());

        Label idleShowLabel = new Label(processGroup, SWT.HORIZONTAL);
        idleShowLabel.setText(resourceBundle.getString("show_idle_process"));
        GridData labelGd = new GridData(GridData.FILL_HORIZONTAL);
        labelGd.horizontalSpan = 2;
        idleShowLabel.setLayoutData(labelGd);

        showIdleButton = new Button(processGroup, SWT.CHECK);
        showIdleButton.setSelection(settings.getShowIdle());

        Label showBackendPidLabel = new Label(processGroup, SWT.HORIZONTAL);
        showBackendPidLabel.setText(resourceBundle.getString("show_own_process"));
        showBackendPidLabel.setLayoutData(labelGd);

        showBackendPidButton = new Button(processGroup, SWT.CHECK);
        showBackendPidButton.setSelection(settings.getShowBackendPid());

        Label limitBlocksLabel = new Label(processGroup, SWT.HORIZONTAL);
        limitBlocksLabel.setText(resourceBundle.getString("limit_block_process"));

        GridData blockGd = new GridData(GridData.FILL_HORIZONTAL);
        blockGd.horizontalSpan = 2;
        blockGd.horizontalIndent = 50;
        limitBlock = new Spinner(processGroup, SWT.BORDER);
        limitBlock.setLayoutData(textGd);
        limitBlock.setMinimum(0);
        limitBlock.setMaximum(10000);
        limitBlock.setSelection(settings.getLimitBlocks());
    }

    private void populateNotificationGroup(Composite container) {
        Group notificationGroup = createGroup(container, resourceBundle.getString("notifications"), 2);

        Label idleShowToolTip = new Label(notificationGroup, SWT.HORIZONTAL);
        idleShowToolTip.setText(resourceBundle.getString("show_tray_notifications"));
        idleShowToolTip.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        showToolTip = new Button(notificationGroup, SWT.CHECK);
        showToolTip.setSelection(settings.getShowToolTip());

        Label confirmRequiredLabel = new Label(notificationGroup, SWT.HORIZONTAL);
        confirmRequiredLabel.setText(resourceBundle.getString("prompt_confirmation_on_process_kill"));
        confirmRequiredLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        confirmRequiredButton = new Button(notificationGroup, SWT.CHECK);
        confirmRequiredButton.setSelection(settings.isConfirmRequired());

        Label confirmExitLabel = new Label(notificationGroup, SWT.HORIZONTAL);
        confirmExitLabel.setText(resourceBundle.getString("prompt_confirmation_on_program_close"));
        confirmExitLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        confirmExitButton = new Button(notificationGroup, SWT.CHECK);
        confirmExitButton.setSelection(settings.isConfirmExit());
    }

    private void populateBlockJournalPathGroup(Composite container) {
        Group generalGroup = createGroup(container, resourceBundle.getString("path"), 2);

        textPath = new Text(generalGroup, SWT.BORDER);
        textPath.setText(settings.getBlocksJournalPath());
        textPath.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        Button btnDir = new Button(generalGroup, SWT.PUSH);
        btnDir.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true));
        btnDir.setImage(ImageUtils.getImage(Images.FOLDER));
        btnDir.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                dialog.setText(resourceBundle.getString("choose_dir"));
                dialog.setFilterPath(PathBuilder.getInstance().getBlocksJournalsDir().toString());
                String path = dialog.open();
                if (path != null) {
                    textPath.setText(path);
                }
            }
        });
    }

    private Group createGroup(Composite container, String name, int numColumns) {
        Group group = new Group(container, SWT.SHADOW_IN);
        group.setText(name);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        group.setLayout(new GridLayout(numColumns, false));
        return group;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(resourceBundle.getString("settings"));
    }

    @Override
    protected void okPressed() {
        settings.setUpdatePeriodSeconds(updatePeriod.getSelection());
        settings.setShowIdle(showIdleButton.getSelection());
        settings.setShowToolTip(showToolTip.getSelection());
        settings.setLimitBlocks(limitBlock.getSelection());
        settings.setConfirmRequired(confirmRequiredButton.getSelection());
        settings.setConfirmExit(confirmExitButton.getSelection());
        settings.setLanguage(languageCombo.getText());
        settings.setShowBackendPid(showBackendPidButton.getSelection());
        settings.setBlocksJournalsPath(textPath.getText());

        super.okPressed();
    }
}
