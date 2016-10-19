package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import ru.taximaxim.pgsqlblocks.utils.Settings;

public class SettingsDlg extends Dialog {

    private Settings settings;
    private Spinner updatePeriod;
    
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
      
      Label nameLabel = new Label(container, SWT.HORIZONTAL);
      nameLabel.setText("Период обновления");
      updatePeriod = new Spinner(container, SWT.BORDER);
      updatePeriod.setLayoutData(textGd);
      updatePeriod.setMinimum(1);
      updatePeriod.setMaximum(100);
      updatePeriod.setSelection(settings.getUpdatePeriod());

      return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Настройки");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(300, 125);
    }
    
    @Override
    protected void okPressed() {
        settings.setUpdatePeriod(updatePeriod.getSelection());

        super.okPressed();
    }
}
