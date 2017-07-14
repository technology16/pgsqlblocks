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
package ru.taximaxim.pgsqlblocks.blocksjournal;

import org.eclipse.jface.viewers.ITreeContentProvider;
import ru.taximaxim.pgsqlblocks.process.Process;

import java.util.List;

public class BlocksJournalContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
            List<BlocksJournalProcess> elements = (List<BlocksJournalProcess>) inputElement;
            return elements.toArray();
        }
        if (inputElement instanceof BlocksJournalProcess) {
            BlocksJournalProcess process = (BlocksJournalProcess) inputElement;
            return process.getProcess().getChildren().toArray();
        }
        if (inputElement instanceof Process) {
            Process process = (Process) inputElement;
            return process.getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof BlocksJournalProcess) {
            BlocksJournalProcess process = (BlocksJournalProcess) parentElement;
            return process.getProcess().getChildren().toArray();
        }
        if (parentElement instanceof Process) {
            Process process = (Process) parentElement;
            return process.getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof BlocksJournalProcess) {
            return true;
        }
        if (element instanceof Process) {
            Process process = (Process) element;
            return process.hasChildren();
        }
        return false;
    }
}
