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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DBProcessesViewDataSource extends TMTreeViewerDataSource {

    private final DateUtils dateUtils = new DateUtils();

    public DBProcessesViewDataSource(ResourceBundle resourceBundle, TMTreeViewerDataSourceFilter<DBProcess> dataFilter) {
        super(resourceBundle, dataFilter);
    }


    @Override
    public List<Columns> getColumns() {
        List<Columns> list = new ArrayList<>(Arrays.asList(Columns.values()));
        list.remove(Columns.CREATE_DATE);
        list.remove(Columns.CLOSE_DATE);
        return list;
    }

    @Override
    public boolean columnIsSortable() {
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
         return getColumnText(element, getColumns().get(columnIndex));
    }


    private String getColumnText(Object element, Columns column) {
        DBProcess process = (DBProcess)element;
        switch (column) {
            case PID:
                return String.valueOf(process.getPid());
            case BLOCKED_COUNT:
                return String.valueOf(process.getChildren().size());
            case APPLICATION_NAME:
                return process.getQueryCaller().getApplicationName();
            case DATABASE_NAME:
                return process.getQueryCaller().getDatabaseName();
            case USER_NAME:
                return process.getQueryCaller().getUserName();
            case CLIENT:
                return process.getQueryCaller().getClient();
            case BACKEND_START:
                return dateUtils.dateToString(process.getQuery().getBackendStart());
            case QUERY_START:
                return dateUtils.dateToString(process.getQuery().getQueryStart());
            case XACT_START:
                return dateUtils.dateToString(process.getQuery().getXactStart());
            case STATE:
                return process.getState();
            case STATE_CHANGE:
                return dateUtils.dateToString(process.getStateChange());
            case BLOCKED:
                return process.getBlocksPidsString();
            case LOCK_TYPE:
                return process.getBlocksLocktypesString();
            case RELATION:
                return process.getBlocksRelationsString();
            case SLOW_QUERY:
                return String.valueOf(process.getQuery().isSlowQuery());
            case QUERY:
                String query = process.getQuery().getQueryString();
                int indexOfNewLine = query.indexOf('\n');
                String substring = query.substring(0, query.indexOf('\n') >= 0 ? indexOfNewLine : query.length());
                return query.indexOf('\n') >= 0 ? substring + " ..." : substring;
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
