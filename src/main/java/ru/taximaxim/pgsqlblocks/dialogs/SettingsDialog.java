package ru.taximaxim.pgsqlblocks.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.util.ResourceBundle;

public class SettingsDialog extends Dialog {

    private final Settings settings;
    private final ResourceBundle resourceBundle;

    private Spinner updatePeriod;
    private Button showIdleButton;
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
        updatePeriod.setSelection(settings.getUpdatePeriod());

        Label idleShowLabel = new Label(processGroup, SWT.HORIZONTAL);
        idleShowLabel.setText(resourceBundle.getString("show_idle_process"));
        GridData labelGd = new GridData(GridData.FILL_HORIZONTAL);
        labelGd.horizontalSpan = 2;
        idleShowLabel.setLayoutData(labelGd);

        showIdleButton = new Button(processGroup, SWT.CHECK);
        showIdleButton.setSelection(settings.getShowIdle());
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
        settings.setUpdatePeriod(updatePeriod.getSelection());
        settings.setShowIdle(showIdleButton.getSelection());
        settings.setShowToolTip(showToolTip.getSelection());
        settings.setConfirmRequired(confirmRequiredButton.getSelection());
        settings.setConfirmExit(confirmExitButton.getSelection());
        settings.setLanguage(languageCombo.getText());

        super.okPressed();
    }
}
