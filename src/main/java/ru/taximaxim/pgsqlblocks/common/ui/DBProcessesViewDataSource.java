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

public class DBProcessesViewDataSource extends TMTreeViewerDataSource<DBProcess> {

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
    public String getColumnTitle(String name) {
        return resourceBundle.getString(name);
    }

    @Override
    public boolean columnIsSortableAtIndex() {
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
