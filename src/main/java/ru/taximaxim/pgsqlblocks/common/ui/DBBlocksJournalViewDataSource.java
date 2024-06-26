/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.common.ui;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.graphics.Image;

import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;

// FIXME seems wrong to inherit from DBProcessesViewDataSource which is DBProcess-related
public class DBBlocksJournalViewDataSource extends DBProcessesViewDataSource {

    private final boolean isModeProcLimit;
    private int limitBlocks;

    public void setLimitBlocks(int limitBlocks) {
        this.limitBlocks = limitBlocks;
    }

    public DBBlocksJournalViewDataSource(ResourceBundle resourceBundle, boolean isModeProcLimit) {
        super(resourceBundle);
        this.isModeProcLimit = isModeProcLimit;
    }

    @Override
    public List<Columns> getColumns() {
        return Arrays.asList(Columns.values());
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof DBBlocksJournalProcess) {
            DBBlocksJournalProcess process = (DBBlocksJournalProcess) element;
            return super.getColumnImage(process.getProcess(), columnIndex);
        }

        return super.getColumnImage(element, columnIndex);
    }

    @Override
    public String getRowText(Object element, Columns column) {
        if (element instanceof DBBlocksJournalProcess) {
            DBBlocksJournalProcess process = (DBBlocksJournalProcess) element;
            switch (column) {
            case BLOCK_CREATE_DATE:
                return DateUtils.dateToString(process.getCreateDate());
            case BLOCK_END_DATE:
                return DateUtils.dateToString(process.getCloseDate());
            default:
                return super.getRowText(process.getProcess(), column);
            }
        }

        return super.getRowText(element, column);
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<DBBlocksJournalProcess> input = (List<DBBlocksJournalProcess>) inputElement;
        if (isModeProcLimit && limitBlocks != 0 && input.size() > limitBlocks) {
            return input.subList(input.size() - limitBlocks, input.size()).toArray();
        }

        return input.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof DBBlocksJournalProcess) {
            return ((DBBlocksJournalProcess) parentElement).getProcess().getChildren().toArray();
        }

        return ((DBProcess) parentElement).getChildren().toArray();
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof DBProcess) {
            return ((DBProcess) element).hasChildren();
        }

        return element instanceof DBBlocksJournalProcess;
    }

    @Override
    public int compare(Object e1, Object e2, Columns column) {
        if (e1 instanceof DBBlocksJournalProcess) {
            DBBlocksJournalProcess process1 = (DBBlocksJournalProcess) e1;
            DBBlocksJournalProcess process2 = (DBBlocksJournalProcess) e2;
            switch (column) {
            case BLOCK_CREATE_DATE:
                return DateUtils.compareDates(process1.getCreateDate(), process2.getCreateDate());
            case BLOCK_END_DATE:
                return DateUtils.compareDates(process1.getCloseDate(), process2.getCloseDate());
            default:
                return super.compare(process1.getProcess(), process2.getProcess(), column);
            }
        }

        return super.compare(e1, e2, column);
    }
}
