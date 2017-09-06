package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.graphics.Image;
import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;

import java.util.List;
import java.util.ResourceBundle;

// FIXME use enum for columns
public class DBBlocksJournalViewDataSource extends TMTreeViewerDataSource<DBBlocksJournalProcess> {

    private final ResourceBundle resourceBundle;

    public DBBlocksJournalViewDataSource(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @Override
    int numberOfColumns() {
        return 19;
    }

    @Override
    String columnTitleForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return resourceBundle.getString("pid");
            case 1:
                return resourceBundle.getString("block_start_date");
            case 2:
                return resourceBundle.getString("block_change_date");
            case 3:
                return resourceBundle.getString("num_of_blocked_processes");
            case 4:
                return resourceBundle.getString("application");
            case 5:
                return resourceBundle.getString("db_name");
            case 6:
                return resourceBundle.getString("user_name");
            case 7:
                return resourceBundle.getString("client");
            case 8:
                return resourceBundle.getString("backend_start");
            case 9:
                return resourceBundle.getString("query_start");
            case 10:
                return resourceBundle.getString("xact_start");
            case 11:
                return resourceBundle.getString("duration");
            case 12:
                return resourceBundle.getString("state");
            case 13:
                return resourceBundle.getString("state_change");
            case 14:
                return resourceBundle.getString("blocked_by");
            case 15:
                return resourceBundle.getString("lock_type");
            case 16:
                return resourceBundle.getString("relation");
            case 17:
                return resourceBundle.getString("slow_query");
            case 18:
                return resourceBundle.getString("query");
            default:
                return "undefined";
        }
    }

    @Override
    int columnWidthForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return 120;
            case 1:
            case 2:
                return 150;
            case 3:
                return 70;
            case 4:
                return 100;
            case 5:
            case 6:
            case 7:
                return 100;
            case 8:
            case 9:
            case 10:
                return 150;
            case 11:
            case 12:
                return 70;
            case 13:
                return 150;
            case 14:
            case 15:
            case 16:
                return 130;
            case 17:
                return 150;
            case 18:
                return 100;
            default:
                return 110;
        }
    }

    @Override
    boolean columnIsSortableAtIndex(int columnIndex) {
        return false;
    }

    @Override
    String columnTooltipForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "PID";
            case 1:
                return "CREATE_DATE";
            case 2:
                return "CLOSE_DATE";
            case 3:
                return "BLOCKED_COUNT";
            case 4:
                return "APPLICATION_NAME";
            case 5:
                return "DATABASE_NAME";
            case 6:
                return "USER_NAME";
            case 7:
                return "CLIENT";
            case 8:
                return "BACKEND_START";
            case 9:
                return "QUERY_START";
            case 10:
                return "XACT_START";
            case 11:
                return resourceBundle.getString("duration");
            case 12:
                return "STATE";
            case 13:
                return "STATE_CHANGE";
            case 14:
                return "BLOCKED";
            case 15:
                return "LOCK_TYPE";
            case 16:
                return "RELATION";
            case 17:
                return "SLOW_QUERY";
            case 18:
                return "QUERY";
            default:
                return "UNDEFINED";
        }
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex != 0) {
            return null;
        }
        if (element instanceof DBBlocksJournalProcess) {
            DBBlocksJournalProcess process = (DBBlocksJournalProcess)element;
            return ImageUtils.getImage(process.getProcess().getStatus().getStatusImage());
        } else {
            DBProcess process = (DBProcess)element;
            return ImageUtils.getImage(process.getStatus().getStatusImage());
        }
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        DBProcess process = null;
        if (element instanceof DBBlocksJournalProcess) {
            DBBlocksJournalProcess parentProcess = (DBBlocksJournalProcess)element;
            process = parentProcess.getProcess();
            switch (columnIndex) {
                case 1:
                    return DateUtils.dateToString(parentProcess.getCreateDate());
                case 2:
                    return DateUtils.dateToString(parentProcess.getCloseDate());
            }
        } else {
            process = (DBProcess)element;
        }
        switch (columnIndex) {
            case 0:
                return String.valueOf(process.getPid());
            case 1:
                return "";
            case 2:
                return "";
            case 3:
                return String.valueOf(process.getChildren().size());
            case 4:
                return process.getQueryCaller().getApplicationName();
            case 5:
                return process.getQueryCaller().getDatabaseName();
            case 6:
                return process.getQueryCaller().getUserName();
            case 7:
                return process.getQueryCaller().getClient();
            case 8:
                return DateUtils.dateToString(process.getQuery().getBackendStart());
            case 9:
                return DateUtils.dateToString(process.getQuery().getQueryStart());
            case 10:
                return DateUtils.dateToString(process.getQuery().getXactStart());
            case 11:
                return DateUtils.durationToString(process.getQuery().getDuration());
            case 12:
                return process.getState();
            case 13:
                return DateUtils.dateToString(process.getStateChange());
            case 14:
                return process.getBlocksPidsString();
            case 15:
                return process.getBlocksLocktypesString();
            case 16:
                return process.getBlocksRelationsString();
            case 17:
                return String.valueOf(process.getQuery().isSlowQuery());
            case 18:
                return process.getQuery().getQueryString();
            default:
                return "UNDEFINED";
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<DBBlocksJournalProcess> input = (List<DBBlocksJournalProcess>) inputElement;
        return input.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof DBBlocksJournalProcess) {
            return ((DBBlocksJournalProcess)parentElement).getProcess().getChildren().toArray();
        } else {
            return ((DBProcess)parentElement).getChildren().toArray();
        }
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof DBBlocksJournalProcess) {
            return true;
        }
        return ((DBProcess)element).hasChildren();
    }
}
