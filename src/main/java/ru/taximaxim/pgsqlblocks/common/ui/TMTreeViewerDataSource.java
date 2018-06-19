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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import ru.taximaxim.pgsqlblocks.utils.Columns;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class TMTreeViewerDataSource<T> implements ITableLabelProvider, ITreeContentProvider {

    protected TMTreeViewerDataSourceFilter<T> dataFilter;
    protected final ResourceBundle resourceBundle;

    public TMTreeViewerDataSource(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public TMTreeViewerDataSource(ResourceBundle resourceBundle, TMTreeViewerDataSourceFilter<T> dataFilter) {
        this.resourceBundle = resourceBundle;
        this.dataFilter = dataFilter;
    }

    public void setDataFilter(TMTreeViewerDataSourceFilter<T> dataFilter) {
        this.dataFilter = dataFilter;
    }

    protected List<ILabelProviderListener> listeners = new ArrayList<>();

    public abstract int numberOfColumns();

    public abstract boolean columnIsSortableAtIndex();

    public abstract List<Columns> getColumns();

    public abstract String columnTitle(String name);

    @Override
    public void dispose() {

    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }
}
