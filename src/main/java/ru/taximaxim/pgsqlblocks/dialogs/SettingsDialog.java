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
package ru.taximaxim.pgsqlblocks.dialogs;

import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

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
        return container;
    }

    private void populateGeneralGroup(Composite container) {
        Group generalGroup = new Group(container, SWT.SHADOW_IN);
        generalGroup.setText(resourceBundle.getString("general"));
        generalGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        generalGroup.setLayout(new GridLayout(2, false));

        Label selectLocale = new Label(generalGroup, SWT.HORIZONTAL);
        selectLocale.setText(resourceBundle.getString("select_ui_language"));
        selectLocale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        languageCombo = new Combo (generalGroup, SWT.READ_ONLY);
        languageCombo.setItems(Settings.SUPPORTED_LANGUAGES);
        languageCombo.select(languageCombo.indexOf(settings.getLocale().getLanguage()));
    }

    private void populateProcessGroup(Composite container) {
        Group processGroup = new Group(container, SWT.SHADOW_IN);
        processGroup.setText(resourceBundle.getString("processes"));
        processGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        processGroup.setLayout(new GridLayout(3, false));

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
        Group notificationGroup = new Group(container, SWT.SHADOW_IN);
        notificationGroup.setText(resourceBundle.getString("notifications"));
        notificationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        notificationGroup.setLayout(new GridLayout(2, false));

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

        super.okPressed();
    }
}
