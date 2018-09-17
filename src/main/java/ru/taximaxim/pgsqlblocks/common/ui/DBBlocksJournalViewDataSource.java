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
import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.DataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DBBlocksJournalViewDataSource extends DataSource<DBBlocksJournalProcess> {

    private final DateUtils dateUtils = new DateUtils();
    private final ResourceBundle bundle;

    public DBBlocksJournalViewDataSource(ResourceBundle resourceBundle) {
        this.bundle = resourceBundle;
    }

    @Override
    public boolean columnIsSortable() {
        return false;
    }

    @Override
    public List<Columns> getColumns() {
        return Arrays.asList(Columns.values());
    }

    @Override
    public List<? extends IColumn> getColumnsToFilter() {
        List<IColumn> list = new ArrayList<>();
        list.add(Columns.PID);
        list.add(Columns.APPLICATION_NAME);
        list.add(Columns.DATABASE_NAME);
        list.add(Columns.QUERY);
        list.add(Columns.USER_NAME);
        list.add(Columns.CLIENT);
        return list;
    }

    @Override
    public ResourceBundle getResourceBundle() {
        return bundle;
    }

    @Override
    public String getLocalizeString(String s) {
        return bundle.getString(s);
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
    public String getRowText(Object element, IColumn column) {
        Columns columns = Columns.getColumn(column);
        DBProcess process;
        DBBlocksJournalProcess parentProcess = null;
        if (element instanceof DBBlocksJournalProcess) {
            parentProcess = (DBBlocksJournalProcess)element;
            process = parentProcess.getProcess();
        }else {
            process = (DBProcess)element;
        }
        switch (columns){
            case PID:
                return String.valueOf(process.getPid());
            case BLOCK_CREATE_DATE:
                return parentProcess != null ? dateUtils.dateToString(parentProcess.getCreateDate()) : "";
            case BLOCK_END_DATE:
                return parentProcess != null ? dateUtils.dateToString(parentProcess.getCloseDate()) : "";
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
            case DURATION:
                return DateUtils.durationToString(process.getQuery().getDuration());
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
                return process.getQuery().getQueryFirstLine();
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
