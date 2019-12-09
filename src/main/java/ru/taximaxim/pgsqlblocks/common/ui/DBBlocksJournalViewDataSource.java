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

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

// FIXME seems wrong to inherit from DBProcessesViewDataSource which is DBProcess-related
public class DBBlocksJournalViewDataSource extends DBProcessesViewDataSource {

    public DBBlocksJournalViewDataSource(ResourceBundle resourceBundle) {
        super(resourceBundle);
    }

    @Override
    public List<Columns> getColumns() {
        return Arrays.asList(Columns.values());
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
        } else {
            process = (DBProcess)element;
        }

        switch (columns) {
            case DURATION:
                return parentProcess != null ? parentProcess.getDuration() : process.getQuery().getDuration();
            case BLOCK_CREATE_DATE:
                return parentProcess != null ? DateUtils.dateToString(parentProcess.getCreateDate()) : "";
            case BLOCK_END_DATE:
                return parentProcess != null ? DateUtils.dateToString(parentProcess.getCloseDate()) : "";
            default:
                return super.getRowText(process, columns);
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
    public boolean hasChildren(Object element) {
        if (element instanceof DBProcess) {
            return ((DBProcess) element).hasChildren();
        } else {
            return element instanceof DBBlocksJournalProcess;
        }
    }
}
