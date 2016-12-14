package ru.taximaxim.pgsqlblocks.ui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ru.taximaxim.pgsqlblocks.SortColumn;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.util.*;
import java.util.List;

public class SettingsDlg extends Dialog {

    private Settings settings;
    private Spinner updatePeriod;
    private Button showIdleButton;
    private SortColumn[] columnsList = SortColumn.values();
    private String[] oldList;
    private Button[] checkBoxes = new Button[columnsList.length];

    public SettingsDlg(Shell shell, Settings settings) {
        super(shell);
        this.settings = settings;
        oldList = settings.getColumnsList().split(",");
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

        for (int i = 0; i < columnsList.length; i++) {
            SortColumn column = columnsList[i];
            checkBoxes[i] = new Button(container, SWT.CHECK);
            checkBoxes[i].setText(column.getName());
            checkBoxes[i].setData(column);
            checkBoxes[i].setSelection(Arrays.stream(oldList)
                    .anyMatch(x -> x.equals(column.toString())));
            checkBoxes[i].setToolTipText(column.toString());
            checkBoxes[i].pack();
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

        List<String> resultList = new ArrayList<String>() {};
        for (Button checkBox : checkBoxes) {
            if (checkBox.getSelection()) {
                resultList.add(checkBox.getData().toString());
            }
        }
        settings.setColumnsList(StringUtils.join(resultList.toArray(), ","));

        super.okPressed();
    }
}
