package ru.taximaxim.pgsqlblocks.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
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
    private static final int TEXT_WIDTH = 200;


    public DBProcessInfoDialog(ResourceBundle resourceBundle, Shell parentShell, Object process) {
        super(parentShell);
        this.dbProcess = process instanceof DBProcess ? ((DBProcess) process) : null;
        this.dbBlocksProcess = process instanceof DBBlocksJournalProcess ? ((DBBlocksJournalProcess) process) : null;
        this.resourceBundle = resourceBundle;
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
            //dbProcess.getPid() pid
            createProcessArea(container, textGd, "pid", String.valueOf(dbProcess.getPid()));
            //dbProcess.getChildren().size() num_of_blocked_processes
            createProcessArea(container, textGd, "num_of_blocked_processes", String.valueOf(dbProcess.getChildren().size()));
            //dbProcess.getQueryCaller().getDatabaseName() db_name
            createProcessArea(container, textGd, "db_name", dbProcess.getQueryCaller().getDatabaseName());
            //dbProcess.getQueryCaller().getApplicationName() application
            createProcessArea(container, textGd, "application", dbProcess.getQueryCaller().getApplicationName());
            //dbProcess.getQueryCaller().getUserName() user_name
            createProcessArea(container, textGd, "user_name", dbProcess.getQueryCaller().getUserName());
            //dbProcess.getQuery().getQueryStart() query_start
            // System.out.println("START "+ DateUtils.dateToString(dbProcess.getQuery().getQueryStart())); //FIXME null need DateUtils but not static
            //createProcessArea(container, textGd, "query_start", dbProcess.getQuery().getQueryStart().toString());
            //dbProcess.getState() state
            createProcessArea(container, textGd, "state", dbProcess.getState());
            //dbProcess.getStatus() status
            //FIXME netu
            //createProcessArea(container, textGd, "status", dbProcess.getStatus().getDescr());
            //System.out.println(dbProcess.getStatus().getDescr());
            //dbProcess.getQuery().getQueryString() query
            createQueryArea(container, dbProcess.getQuery().getQueryString());
        return container;
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
}
