/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
 * %
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
                return resourceBundle.getString("state");
            case 12:
                return resourceBundle.getString("state_change");
            case 13:
                return resourceBundle.getString("blocked_by");
            case 14:
                return resourceBundle.getString("lock_type");
            case 15:
                return resourceBundle.getString("relation");
            case 16:
                return resourceBundle.getString("slow_query");
            case 17:
                return resourceBundle.getString("query");
            default:
                return "undefined";
        }
    }

    @Override
    public int columnWidthForColumnIndex(int columnIndex) {
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
                return 70;
            case 12:
                return 150;
            case 13:
            case 14:
            case 15:
                return 130;
            case 16:
                return 150;
            case 17:
                return 100;
            default:
                return 110;
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
                return "STATE";
            case 12:
                return "STATE_CHANGE";
            case 13:
                return "BLOCKED";
            case 14:
                return "LOCK_TYPE";
            case 15:
                return "RELATION";
            case 16:
                return "SLOW_QUERY";
            case 17:
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
