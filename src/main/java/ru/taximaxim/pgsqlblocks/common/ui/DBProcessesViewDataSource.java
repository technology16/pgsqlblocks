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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.swt.graphics.Image;

import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;
import ru.taximaxim.treeviewer.models.DataSource;

public class DBProcessesViewDataSource extends DataSource<DBProcess> {

    private final ResourceBundle bundle;

    public DBProcessesViewDataSource(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public List<Columns> getColumns() {
        List<Columns> list = new ArrayList<>(Arrays.asList(Columns.values()));
        list.remove(Columns.BLOCK_CREATE_DATE);
        list.remove(Columns.BLOCK_END_DATE);
        return list;
    }

    @Override
    public Set<Columns> getColumnsToFilter() {
        return EnumSet.of(Columns.PID, Columns.APPLICATION_NAME,
                Columns.DATABASE_NAME, Columns.QUERY, Columns.USER_NAME, Columns.CLIENT);
    }

    @Override
    public ResourceBundle getResourceBundle() {
        return bundle;
    }

    @Override
    public String getRowText(Object element, Columns column) {
        DBProcess process = (DBProcess) element;
        switch (column) {
        case PID:
            return String.valueOf(process.getPid());
        case BACKEND_TYPE:
            return process.getBackendType();
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
            return DateUtils.dateToString(process.getQuery().getBackendStart());
        case QUERY_START:
            return DateUtils.dateToString(process.getQuery().getQueryStart());
        case XACT_START:
            return DateUtils.dateToString(process.getQuery().getXactStart());
        case DURATION:
            return process.getQuery().getDuration();
        case STATE:
            return process.getState();
        case STATE_CHANGE:
            return DateUtils.dateToString(process.getStateChange());
        case BLOCKED:
            return process.getBlocksPidsString();
        case LOCK_TYPE:
            return process.getBlocksLocktypesString();
        case RELATION:
            return process.getBlocksRelationsString();
        case SLOW_QUERY:
            return String.valueOf(process.getQuery().isSlowQuery());
        case QUERY:
            return process.getQuery().getQueryFirstLine();
        default:
            return "";
        }
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex != 0) {
            return null;
        }

        DBProcess process = (DBProcess) element;
        return ImageUtils.getImage(process.getStatus().getStatusImage());
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<DBProcess> input = (List<DBProcess>) inputElement;
        return input.toArray();
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

    @Override
    public int compare(Object e1, Object e2, Columns column) {
        DBProcess process1 = (DBProcess) e1;
        DBProcess process2 = (DBProcess) e2;

        switch (column) {
        case PID:
            return Integer.compare(process1.getPid(), process2.getPid());
        case BLOCKED_COUNT:
            return Integer.compare(process1.getChildren().size(), process2.getChildren().size());
        case APPLICATION_NAME:
            return process1.getQueryCaller().getApplicationName().compareTo(
                    process2.getQueryCaller().getApplicationName());
        case DATABASE_NAME:
            return process1.getQueryCaller().getDatabaseName().compareTo(
                    process2.getQueryCaller().getDatabaseName());
        case USER_NAME:
            return process1.getQueryCaller().getUserName().compareTo(
                    process2.getQueryCaller().getUserName());
        case CLIENT:
            return process1.getQueryCaller().getClient().compareTo(
                    process2.getQueryCaller().getClient());
        case BACKEND_START:
            return DateUtils.compareDates(process1.getQuery().getBackendStart(),
                    process2.getQuery().getBackendStart());
        case QUERY_START:
            return DateUtils.compareDates(process1.getQuery().getQueryStart(),
                    process2.getQuery().getQueryStart());
        case XACT_START:
            return DateUtils.compareDates(process1.getQuery().getXactStart(),
                    process2.getQuery().getXactStart());
        case DURATION:
            return process1.getQuery().getDuration().compareTo(
                    process2.getQuery().getDuration());
        case STATE:
            return process1.getState().compareTo(process2.getState());
        case STATE_CHANGE:
            return DateUtils.compareDates(process1.getStateChange(),
                    process2.getStateChange());
        case BLOCKED:
            return process1.getBlocksPidsString().compareTo(
                    process2.getBlocksPidsString());
        case LOCK_TYPE:
            return process1.getBlocksLocktypesString().compareTo(
                    process2.getBlocksLocktypesString());
        case RELATION:
            return process1.getBlocksRelationsString().compareTo(
                    process2.getBlocksRelationsString());
        case QUERY:
            return process1.getQuery().getQueryString().compareTo(
                    process2.getQuery().getQueryString());
        case SLOW_QUERY:
            return Boolean.compare(process1.getQuery().isSlowQuery(),
                    process2.getQuery().isSlowQuery());
        default:
            return 0;
        }
    }
}
