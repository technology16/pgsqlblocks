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

import java.util.ResourceBundle;

/**
 * Dialog for current process info
 */
public class DBProcessInfoDialog extends Dialog{

    private ResourceBundle resourceBundle;
    private DBProcess dbProcess;
    private DBBlocksJournalProcess dbBlocksProcess;
    private ProcessInfoListener processInfoListener;
    private Button cancelButton;
    private Button terminateButton;
    private boolean disabledButton;
    private static final int TEXT_WIDTH = 200;



    public DBProcessInfoDialog(ResourceBundle resourceBundle, Shell parentShell, Object process, ProcessInfoListener listener) {
        super(parentShell);
        this.dbProcess = process instanceof DBProcess ? ((DBProcess) process) : null;
        this.dbBlocksProcess = process instanceof DBBlocksJournalProcess ? ((DBBlocksJournalProcess) process) : null;
        this.resourceBundle = resourceBundle;
        this.processInfoListener = listener;
    }

    public DBProcessInfoDialog(ResourceBundle resourceBundle, Shell parentShell, Object process) {
        super(parentShell);
        this.dbProcess = process instanceof DBProcess ? ((DBProcess) process) : null;
        this.dbBlocksProcess = process instanceof DBBlocksJournalProcess ? ((DBBlocksJournalProcess) process) : null;
        this.resourceBundle = resourceBundle;
        this.processInfoListener = null;
        this.disabledButton = true;
    }

    private void disableButtons() {
        cancelButton.setEnabled(false);
        terminateButton.setEnabled(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        layout.marginTop = 10;
        layout.horizontalSpacing = 2;
        container.setLayout(layout);

        GridData textGd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        textGd.widthHint = TEXT_WIDTH;
        if (dbBlocksProcess != null) {
            dbProcess = dbBlocksProcess.getProcess();
        }
        createProcessArea(container, textGd, "pid", String.valueOf(dbProcess.getPid()));
        createProcessArea(container, textGd, "num_of_blocked_processes", String.valueOf(dbProcess.getChildren().size()));
        createProcessArea(container, textGd, "db_name", dbProcess.getQueryCaller().getDatabaseName());
        createProcessArea(container, textGd, "application", dbProcess.getQueryCaller().getApplicationName());
        createProcessArea(container, textGd, "user_name", dbProcess.getQueryCaller().getUserName());
        // System.out.println("START "+ DateUtils.dateToString(dbProcess.getQuery().getQueryStart())); //FIXME null need DateUtils but not static
        //createProcessArea(container, textGd, "query_start", dbProcess.getQuery().getQueryStart().toString());
        createProcessArea(container, textGd, "state", dbProcess.getState());
        createQueryArea(container, dbProcess.getQuery().getQueryString());
        createButtonArea(container);
        return container;
    }

    private void createButtonArea(Composite container) {
        cancelButton = new Button(container, SWT.PUSH);
        cancelButton.setText("CANCEL");
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

        terminateButton = new Button(container, SWT.PUSH);
        terminateButton.setText("TERMINATE");
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
            disableButtons();
        }
    }

    private void createProcessArea(Composite container, GridData gridData, String type, String data) {
        Label pidLabel = new Label(container, SWT.HORIZONTAL);
        pidLabel.setText(resourceBundle.getString(type));
        Label pid = new Label(container, SWT.LEFT);
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
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 20));
        Label pidLabel = new Label(composite, SWT.HORIZONTAL);
        pidLabel.setText(resourceBundle.getString("query"));

        StyledText pid = new StyledText(composite,  SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        if (data != null) {
            pid.setText(data);
        }else {
            pid.setText("");
        }
        pid.setWordWrap(true);
        GridData grid = new GridData(SWT.FILL, SWT.FILL, false, true);
        grid.widthHint = 500;
        pid.setLayoutData(grid);
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

    public interface ProcessInfoListener {
        void terminateButtonClick();
        void cancelButtonClick();
    }
}
