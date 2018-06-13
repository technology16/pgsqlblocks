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
import ru.taximaxim.pgsqlblocks.utils.Columns;
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
                return resourceBundle.getString(Columns.PID.getColumnName());
            case 1:
                return resourceBundle.getString(Columns.BLOCKED_COUNT.getColumnName());
            case 2:
                return resourceBundle.getString(Columns.APPLICATION_NAME.getColumnName());
            case 3:
                return resourceBundle.getString(Columns.DATABASE_NAME.getColumnName());
            case 4:
                return resourceBundle.getString(Columns.USER_NAME.getColumnName());
            case 5:
                return resourceBundle.getString(Columns.CLIENT.getColumnName());
            case 6:
                return resourceBundle.getString(Columns.BACKEND_START.getColumnName());
            case 7:
                return resourceBundle.getString(Columns.QUERY_START.getColumnName());
            case 8:
                return resourceBundle.getString(Columns.XACT_START.getColumnName());
            case 9:
                return resourceBundle.getString(Columns.STATE.getColumnName());
            case 10:
                return resourceBundle.getString(Columns.STATE_CHANGE.getColumnName());
            case 11:
                return resourceBundle.getString(Columns.BLOCKED.getColumnName());
            case 12:
                return resourceBundle.getString(Columns.LOCK_TYPE.getColumnName());
            case 13:
                return resourceBundle.getString(Columns.RELATION.getColumnName());
            case 14:
                return resourceBundle.getString(Columns.SLOW_QUERY.getColumnName());
            case 15:
                return resourceBundle.getString(Columns.QUERY.getColumnName());
            default:
                return resourceBundle.getString(Columns.UNDEFINED.getColumnName());
        }
    }

    @Override
    public String columnTooltipForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Columns.PID.getColumnTooltip();
            case 1:
                return Columns.BLOCKED_COUNT.getColumnTooltip();
            case 2:
                return Columns.APPLICATION_NAME.getColumnTooltip();
            case 3:
                return Columns.DATABASE_NAME.getColumnTooltip();
            case 4:
                return Columns.USER_NAME.getColumnTooltip();
            case 5:
                return Columns.CLIENT.getColumnTooltip();
            case 6:
                return Columns.BACKEND_START.getColumnTooltip();
            case 7:
                return Columns.QUERY_START.getColumnTooltip();
            case 8:
                return Columns.XACT_START.getColumnTooltip();
            case 9:
                return Columns.STATE.getColumnTooltip();
            case 10:
                return Columns.STATE_CHANGE.getColumnTooltip();
            case 11:
                return Columns.BLOCKED.getColumnTooltip();
            case 12:
                return Columns.LOCK_TYPE.getColumnTooltip();
            case 13:
                return Columns.RELATION.getColumnTooltip();
            case 14:
                return Columns.SLOW_QUERY.getColumnTooltip();
            case 15:
                return Columns.QUERY.getColumnTooltip();
            default:
                return Columns.UNDEFINED.getColumnTooltip();
        }
    }

    @Override
    public int columnWidthForColumnIndex(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Columns.PID.getColumnWidth();
            case 1:
                return Columns.BLOCKED_COUNT.getColumnWidth();
            case 2:
                return Columns.APPLICATION_NAME.getColumnWidth();
            case 3:
            case 4:
            case 5:
                return Columns.CLIENT.getColumnWidth();
            case 6:
            case 7:
            case 8:
                return Columns.XACT_START.getColumnWidth();
            case 9:
                return Columns.STATE.getColumnWidth();
            case 10:
                return Columns.STATE_CHANGE.getColumnWidth();
            case 11:
            case 12:
            case 13:
                return Columns.RELATION.getColumnWidth();
            case 14:
                return Columns.SLOW_QUERY.getColumnWidth();
            case 15:
                return Columns.QUERY.getColumnWidth();
            default:
                return Columns.UNDEFINED.getColumnWidth();
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
