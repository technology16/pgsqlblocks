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

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DBBlocksJournalViewDataSource extends TMTreeViewerDataSource<DBBlocksJournalProcess> {

    private final DateUtils dateUtils = new DateUtils();

    public DBBlocksJournalViewDataSource(ResourceBundle resourceBundle) {
        super(resourceBundle);
    }

    @Override
    public boolean columnIsSortableAtIndex() {
        return false;
    }

    @Override
    public List<Columns> getColumns() {
        return Arrays.asList(Columns.values());
    }

    @Override
    public String getColumnTitle(String name) {
        return resourceBundle.getString(name);
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
