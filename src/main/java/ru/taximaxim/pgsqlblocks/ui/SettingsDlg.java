package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ru.taximaxim.pgsqlblocks.utils.Settings;

public class SettingsDlg extends Dialog {

    private Settings settings;
    private Spinner updatePeriod;
    private Button showIdleButton;
    
    public SettingsDlg(Shell shell, Settings settings) {
        super(shell);
        this.settings = settings;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        container.setLayout(layout);

        GridData textGd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        textGd.widthHint = 100;

        Label updatePeriodLabel = new Label(container, SWT.HORIZONTAL);
        updatePeriodLabel.setText("Период обновления");
        updatePeriod = new Spinner(container, SWT.BORDER);
        updatePeriod.setLayoutData(textGd);
        updatePeriod.setMinimum(1);
        updatePeriod.setMaximum(100);
        updatePeriod.setSelection(settings.getUpdatePeriod());

        Label idleShowLabel = new Label(container, SWT.HORIZONTAL);
        idleShowLabel.setText("Показывать idle процессы");
        showIdleButton = new Button(container, SWT.CHECK);
        showIdleButton.setSelection(settings.getShowIdle());

      return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Настройки");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(325, 175);
    }
    
    @Override
    protected void okPressed() {
        settings.setUpdatePeriod(updatePeriod.getSelection());
        settings.setShowIdle(showIdleButton.getSelection());

        super.okPressed();
    }
}
