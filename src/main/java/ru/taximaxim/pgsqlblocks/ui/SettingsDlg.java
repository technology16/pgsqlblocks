package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ru.taximaxim.pgsqlblocks.SortColumn;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.util.*;

public class SettingsDlg extends Dialog {

    private Settings settings;
    private Spinner updatePeriod;
    private Button showIdleButton;
    private Set<SortColumn> enabledColumns = new HashSet<>();

    public SettingsDlg(Shell shell, Settings settings) {
        super(shell);
        this.settings = settings;
        enabledColumns.addAll(settings.getColumnsList());
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
        updatePeriodLabel.setText("Период обновления:");
        updatePeriod = new Spinner(container, SWT.BORDER);
        updatePeriod.setLayoutData(textGd);
        updatePeriod.setMinimum(1);
        updatePeriod.setMaximum(100);
        updatePeriod.setSelection(settings.getUpdatePeriod());

        Label idleShowLabel = new Label(container, SWT.HORIZONTAL);
        idleShowLabel.setText("Показывать idle процессы:");
        showIdleButton = new Button(container, SWT.CHECK);
        showIdleButton.setSelection(settings.getShowIdle());

        Label columnsLabel = new Label(container, SWT.HORIZONTAL);
        columnsLabel.setText("Отображаемые колонки: ");
        final Sash sash = new Sash(container, SWT.HORIZONTAL);
        sash.setVisible(true);

        for (SortColumn column : SortColumn.values()) {
            Button newBtn = new Button(container, SWT.CHECK);
            newBtn.setText(column.getName());
            newBtn.setData(column);
            newBtn.setSelection(enabledColumns.contains(column));
            newBtn.setToolTipText(column.toString());
            newBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Button button = ((Button) e.getSource());
                    if (button.getSelection()) {
                        enabledColumns.add((SortColumn)button.getData());
                    } else {
                        enabledColumns.remove((SortColumn)button.getData());
                    }
                }
            });
        }

        container.pack();
        return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Настройки");
    }

    @Override
    protected void okPressed() {
        settings.setUpdatePeriod(updatePeriod.getSelection());
        settings.setShowIdle(showIdleButton.getSelection());
        settings.setColumnsList(enabledColumns);

        super.okPressed();
    }
}
