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
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DBProcessesViewDataSource extends TMTreeViewerDataSource<DBProcess> {

    private final DateUtils dateUtils = new DateUtils();

    public DBProcessesViewDataSource(ResourceBundle resourceBundle, TMTreeViewerDataSourceFilter<DBProcess> dataFilter) {
        super(resourceBundle, dataFilter);
    }

    @Override
    public int numberOfColumns() {
        return 16;
    }

    @Override
    public String columnTitleForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return resourceBundle.getString("pid");
            case 1:
                return resourceBundle.getString("num_of_blocked_processes");
            case 2:
                return resourceBundle.getString("application");
            case 3:
                return resourceBundle.getString("db_name");
            case 4:
                return resourceBundle.getString("user_name");
            case 5:
                return resourceBundle.getString("client");
            case 6:
                return resourceBundle.getString("backend_start");
            case 7:
                return resourceBundle.getString("query_start");
            case 8:
                return resourceBundle.getString("xact_start");
            case 9:
                return resourceBundle.getString("state");
            case 10:
                return resourceBundle.getString("state_change");
            case 11:
                return resourceBundle.getString("blocked_by");
            case 12:
                return resourceBundle.getString("lock_type");
            case 13:
                return resourceBundle.getString("relation");
            case 14:
                return resourceBundle.getString("slow_query");
            case 15:
                return resourceBundle.getString("query");
            default:
                return resourceBundle.getString("undefined");
        }
    }

    @Override
    public String columnTooltipForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "PID";
            case 1:
                return "BLOCKED_COUNT";
            case 2:
                return "APPLICATION_NAME";
            case 3:
                return "DATABASE_NAME";
            case 4:
                return "USER_NAME";
            case 5:
                return "CLIENT";
            case 6:
                return "BACKEND_START";
            case 7:
                return "QUERY_START";
            case 8:
                return "XACT_START";
            case 9:
                return "STATE";
            case 10:
                return "STATE_CHANGE";
            case 11:
                return "BLOCKED";
            case 12:
                return "LOCK_TYPE";
            case 13:
                return "RELATION";
            case 14:
                return "SLOW_QUERY";
            case 15:
                return "QUERY";
            default:
                return "UNDEFINED";
        }
    }

    @Override
    public int columnWidthForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return 80;
            case 1:
                return 70;
            case 2:
                return 100;
            case 3:
            case 4:
            case 5:
                return 100;
            case 6:
            case 7:
            case 8:
                return 150;
            case 9:
                return 70;
            case 10:
                return 150;
            case 11:
            case 12:
            case 13:
                return 130;
            case 14:
                return 150;
            case 15:
                return 100;
            default:
                return 110;
        }
    }

    @Override
    public boolean columnIsSortableAtIndex(int columnIndex) {
        return true;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        DBProcess process = (DBProcess)element;
        if (columnIndex == 0) {
            return ImageUtils.getImage(process.getStatus().getStatusImage());
        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        DBProcess process = (DBProcess)element;
        switch (columnIndex) {
            case 0:
                return String.valueOf(process.getPid());
            case 1:
                return String.valueOf(process.getChildren().size());
            case 2:
                return process.getQueryCaller().getApplicationName();
            case 3:
                return process.getQueryCaller().getDatabaseName();
            case 4:
                return process.getQueryCaller().getUserName();
            case 5:
                return process.getQueryCaller().getClient();
            case 6:
                return dateUtils.dateToString(process.getQuery().getBackendStart());
            case 7:
                return dateUtils.dateToString(process.getQuery().getQueryStart());
            case 8:
                return dateUtils.dateToString(process.getQuery().getXactStart());
            case 9:
                return process.getState();
            case 10:
                return dateUtils.dateToString(process.getStateChange());
            case 11:
                return process.getBlocksPidsString();
            case 12:
                return process.getBlocksLocktypesString();
            case 13:
                return process.getBlocksRelationsString();
            case 14:
                return String.valueOf(process.getQuery().isSlowQuery());
            case 15:
                String query = process.getQuery().getQueryString();
                int indexOfNewLine = query.indexOf("\n");
                String substring = query.substring(0, query.indexOf("\n") >= 0 ? indexOfNewLine : query.length());
                return query.indexOf("\n") >= 0 ? substring + " ..." : substring;
            default:
                return "UNDEFINED";
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<DBProcess> input = (List<DBProcess>) inputElement;
        if (dataFilter == null) {
            return input.toArray();
        } else {
            return filterInput(input).toArray();
        }
    }

    private List<DBProcess> filterInput(List<DBProcess> input) {
        return input.stream().filter(process -> dataFilter.filter(process)).collect(Collectors.toList());
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        DBProcess process = (DBProcess)parentElement;
        return process.getChildren().toArray();
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        DBProcess process = (DBProcess)element;
        return process.hasChildren();
    }
}
