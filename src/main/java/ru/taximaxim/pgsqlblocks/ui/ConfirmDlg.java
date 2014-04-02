package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import ru.taximaxim.pgsqlblocks.DbcData;
import ru.taximaxim.pgsqlblocks.DbcDataList;

public class ConfirmDlg {

    private Shell shell;
    private Shell parent;
    private Label label;
    private Button onOk;
    private Button onCancel;
    private Action action;
    private DbcData deletableDbcData;

    private enum Action{
        DELETEDBC,
    }

    public ConfirmDlg(Shell parent){
        this.parent = parent;
        createControls();
        addHandlers();
    }

    private void createControls() {
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setLayout(new GridLayout(2, true));
        shell.setText("Подтверждение действия");
        Composite labelComposite = new Composite(shell, SWT.NONE);
        labelComposite.setLayout(new GridLayout());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 2;
        gridData.heightHint = 100;
        labelComposite.setLayoutData(gridData);
        label = new Label(labelComposite, SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        
        onOk = new Button(shell, SWT.PUSH);
        GridData okGd = new GridData(SWT.BEGINNING, SWT.NONE,true, false);
        okGd.widthHint = MainForm.BTN_WIDTH;
        onOk.setLayoutData(okGd);
        onOk.setText("Ок");

        onCancel = new Button(shell, SWT.PUSH);
        GridData cancelGd = new GridData(SWT.END, SWT.NONE,true, false);
        cancelGd.widthHint = MainForm.BTN_WIDTH;
        onCancel.setLayoutData(cancelGd);
        onCancel.setText("Отмена");

        shell.pack();
    }

    private void addHandlers() {
        Listener closeListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.doit = false;
                hide();
            }
        };
        shell.addListener(SWT.Traverse, closeListener);
        shell.addListener(SWT.Close, closeListener);
        onCancel.addListener(SWT.Selection, closeListener);
        onOk.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event event) {
                switch (action) {
                case DELETEDBC:
                    DbcDataList.getInstance().delete(deletableDbcData);
                    MainForm.getInstance().serverListUpdate();
                    MainForm.getInstance().deleteServer(deletableDbcData);
                    hide();
                    break;
                }
            }
        });
    }

    public void show() {
        shell.open();
    }

    public void show(DbcData dbcData) {
        action = Action.DELETEDBC;
        deletableDbcData = dbcData;
        label.setText("Удалить " + deletableDbcData.getName() + "?");
        shell.open();
    }

    public void hide() {
        shell.setVisible(false);
        label.setText("");
    }
}
