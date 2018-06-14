/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.graphics.Image;
import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;

import java.util.List;
import java.util.ResourceBundle;

public class DBBlocksJournalViewDataSource extends TMTreeViewerDataSource<DBBlocksJournalProcess> {

    private final DateUtils dateUtils = new DateUtils();

    public DBBlocksJournalViewDataSource(ResourceBundle resourceBundle) {
        super(resourceBundle);
    }

    @Override
    public int numberOfColumns() {
        return 18;
    }

    @Override
    public String columnTitleForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return resourceBundle.getString(Columns.PID.getColumnName());
            case 1:
                return resourceBundle.getString(Columns.CREATE_DATE.getColumnName());
            case 2:
                return resourceBundle.getString(Columns.CLOSE_DATE.getColumnName());
            case 3:
                return resourceBundle.getString(Columns.BLOCKED_COUNT.getColumnName());
            case 4:
                return resourceBundle.getString(Columns.APPLICATION_NAME.getColumnName());
            case 5:
                return resourceBundle.getString(Columns.DATABASE_NAME.getColumnName());
            case 6:
                return resourceBundle.getString(Columns.USER_NAME.getColumnName());
            case 7:
                return resourceBundle.getString(Columns.CLIENT.getColumnName());
            case 8:
                return resourceBundle.getString(Columns.BACKEND_START.getColumnName());
            case 9:
                return resourceBundle.getString(Columns.QUERY_START.getColumnName());
            case 10:
                return resourceBundle.getString(Columns.XACT_START.getColumnName());
            case 11:
                return resourceBundle.getString(Columns.STATE.getColumnName());
            case 12:
                return resourceBundle.getString(Columns.STATE_CHANGE.getColumnName());
            case 13:
                return resourceBundle.getString(Columns.BLOCKED.getColumnName());
            case 14:
                return resourceBundle.getString(Columns.LOCK_TYPE.getColumnName());
            case 15:
                return resourceBundle.getString(Columns.RELATION.getColumnName());
            case 16:
                return resourceBundle.getString(Columns.SLOW_QUERY.getColumnName());
            case 17:
                return resourceBundle.getString(Columns.QUERY.getColumnName());
            default:
                return Columns.UNDEFINED.getColumnName();
        }
    }

    @Override
    public int columnWidthForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return 120; //PID Columns.PID.getColumnWidth()
            case 1:
            case 2:
                return Columns.CLOSE_DATE.getColumnWidth();
            case 3:
                return Columns.BLOCKED_COUNT.getColumnWidth();
            case 4:
                return Columns.APPLICATION_NAME.getColumnWidth();
            case 5:
            case 6:
            case 7:
                return Columns.CLIENT.getColumnWidth();
            case 8:
            case 9:
            case 10:
                return Columns.XACT_START.getColumnWidth();
            case 11:
                return Columns.STATE.getColumnWidth();
            case 12:
                return Columns.STATE_CHANGE.getColumnWidth();
            case 13:
            case 14:
            case 15:
                return Columns.RELATION.getColumnWidth();
            case 16:
                return Columns.SLOW_QUERY.getColumnWidth();
            case 17:
                return Columns.QUERY.getColumnWidth();
            default:
                return Columns.UNDEFINED.getColumnWidth();
        }
    }

    @Override
    public boolean columnIsSortableAtIndex(int columnIndex) {
        return false;
    }

    @Override
    public String columnTooltipForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Columns.PID.getColumnTooltip();
            case 1:
                return Columns.CREATE_DATE.getColumnTooltip();
            case 2:
                return Columns.CLOSE_DATE.getColumnTooltip();
            case 3:
                return Columns.BLOCKED_COUNT.getColumnTooltip();
            case 4:
                return Columns.APPLICATION_NAME.getColumnTooltip();
            case 5:
                return Columns.DATABASE_NAME.getColumnTooltip();
            case 6:
                return Columns.USER_NAME.getColumnTooltip();
            case 7:
                return Columns.CLIENT.getColumnTooltip();
            case 8:
                return Columns.BACKEND_START.getColumnTooltip();
            case 9:
                return Columns.QUERY_START.getColumnTooltip();
            case 10:
                return Columns.XACT_START.getColumnTooltip();
            case 11:
                return Columns.STATE.getColumnTooltip();
            case 12:
                return Columns.STATE_CHANGE.getColumnTooltip();
            case 13:
                return Columns.BLOCKED.getColumnTooltip();
            case 14:
                return Columns.LOCK_TYPE.getColumnTooltip();
            case 15:
                return Columns.RELATION.getColumnTooltip();
            case 16:
                return Columns.SLOW_QUERY.getColumnTooltip();
            case 17:
                return Columns.QUERY.getColumnTooltip();
            default:
                return Columns.UNDEFINED.getColumnTooltip();
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
        DBProcess process;
        if (element instanceof DBBlocksJournalProcess) {
            DBBlocksJournalProcess parentProcess = (DBBlocksJournalProcess)element;
            process = parentProcess.getProcess();
            switch (columnIndex) {
                case 1:
                    return dateUtils.dateToString(parentProcess.getCreateDate());
                case 2:
                    return dateUtils.dateToString(parentProcess.getCloseDate());
                default:
                    // no-op
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
                return dateUtils.dateToString(process.getQuery().getBackendStart());
            case 9:
                return dateUtils.dateToString(process.getQuery().getQueryStart());
            case 10:
                return dateUtils.dateToString(process.getQuery().getXactStart());
            case 11:
                return process.getState();
            case 12:
                return dateUtils.dateToString(process.getStateChange());
            case 13:
                return process.getBlocksPidsString();
            case 14:
                return process.getBlocksLocktypesString();
            case 15:
                return process.getBlocksRelationsString();
            case 16:
                return String.valueOf(process.getQuery().isSlowQuery());
            case 17:
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
        if (element instanceof DBProcess) {
            return ((DBProcess) element).hasChildren();
        } else {
            return element instanceof DBBlocksJournalProcess;
        }
    }
}
