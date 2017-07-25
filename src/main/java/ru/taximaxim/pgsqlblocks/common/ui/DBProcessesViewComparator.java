package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;

public class DBProcessesViewComparator extends ViewerComparator {

    private final int columnIndex;

    private final int sortDirection;

    public DBProcessesViewComparator(int columnIndex, int sortDirection) {
        this.columnIndex = columnIndex;
        this.sortDirection = sortDirection;
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int compareResult = 0;
        DBProcess process1 = (DBProcess) e1;
        DBProcess process2 = (DBProcess) e2;
        switch (columnIndex) {
            case 0:
                compareResult = compareIntegerValues(process1.getPid(), process2.getPid());
                break;
            case 1:
                compareResult = compareIntegerValues(process1.getChildren().size(), process2.getChildren().size());
                break;
            case 2:
                compareResult = compareStringValues(process1.getQueryCaller().getApplicationName(),
                        process2.getQueryCaller().getApplicationName());
                break;
            case 3:
                compareResult = compareStringValues(process1.getQueryCaller().getDatabaseName(),
                        process2.getQueryCaller().getDatabaseName());
                break;
            case 4:
                compareResult = compareStringValues(process1.getQueryCaller().getUserName(),
                        process2.getQueryCaller().getUserName());
                break;
            case 5:
                compareResult = compareStringValues(process1.getQueryCaller().getClient(),
                        process2.getQueryCaller().getClient());
                break;
            case 6:
                compareResult = DateUtils.compareDates(process1.getQuery().getBackendStart(),
                        process2.getQuery().getBackendStart());
                break;
            case 7:
                compareResult = DateUtils.compareDates(process1.getQuery().getQueryStart(),
                        process2.getQuery().getQueryStart());
                break;
            case 8:
                compareResult = DateUtils.compareDates(process1.getQuery().getXactStart(),
                        process2.getQuery().getXactStart());
                break;
            case 9:
                compareResult = compareStringValues(process1.getState(), process2.getState());
                break;
            case 10:
                compareResult = DateUtils.compareDates(process1.getStateChange(), process2.getStateChange());
                break;
            case 11:
                compareResult = compareStringValues(process1.getBlocksPidsString(), process2.getBlocksPidsString());
                break;
            case 12:
                compareResult = compareStringValues(process1.getBlocksLocktypesString(),
                        process2.getBlocksLocktypesString());
                break;
            case 13:
                compareResult = compareStringValues(process1.getBlocksRelationsString(),
                        process2.getBlocksRelationsString());
                break;
            case 14:
                compareResult = compareStringValues(process1.getQuery().getQueryString(),
                        process2.getQuery().getQueryString());
                break;
            case 15:
                compareResult = compareBooleans(process1.getQuery().isSlowQuery(), process2.getQuery().isSlowQuery());
                break;

        }
        return sortDirection == SWT.DOWN ? compareResult : -compareResult;
    }

    private int compareIntegerValues(int i1, int i2) {
        Integer integer1 = i1;
        Integer integer2 = i2;
        return integer1.compareTo(integer2);
    }

    private int compareStringValues(String s1, String s2) {
        return s1.compareTo(s2);
    }

    private int compareBooleans(boolean b1, boolean b2) {
        return b1 == b2 ? 0 : b1 ? 1 : -1;
    }

}
