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
package ru.taximaxim.treeviewer.models;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import ru.taximaxim.pgsqlblocks.utils.Columns;

/**
 * Класс, выполняющий работу с формированием таблиц и данных.<br>
 * От него необходимо будет унаследоваться и переопределить методы для получения текста колонок<br><br>
 * getElements должен превращать список данных для treeViewerа;<br>
 * getChildren должен возвращать список дочерних объектов того же типа;<br>
 * getParent должен возвращать родительский объект, если нужно, чаще null;<br>
 * hasChildren должен возвращать true если у объекта есть дочерние объекты;<br>
 * **********************************************************************<br>
 * getColumns возвращает список объектов, где объект колонки имплементит IColumn;<br>
 * getColumnImage получить для строки объекта изображение для определенной колонки<br>
 * getColumnText получить значение для определенной ячейки. сделать метод getRowText(element, getColumns().get(columnIndex))<br>
 * для возможно проходить не по индексу колонки, а по самой колонке<br>
 * localizeString позволяет получить строку из resourceBundle
 */
public abstract class DataSource<T extends IObject> implements ITableLabelProvider, ITreeContentProvider {

    protected List<ILabelProviderListener> listeners = new ArrayList<>();

    public abstract List<Columns> getColumns();

    public abstract Set<Columns> getColumnsToFilter();

    public abstract ResourceBundle getResourceBundle();

    public abstract String getRowText(Object element, Columns column);

    public abstract int compare(Object e1, Object e2, Columns column);

    public String getLocalizeString(String name) {
        if (getResourceBundle() == null || !getResourceBundle().containsKey(name)) {
            return name;
        }

        return getResourceBundle().getString(name);
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        return getRowText(element, getColumns().get(columnIndex));
    }

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
