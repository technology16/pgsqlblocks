/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2018 "Technology" LLC
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
package ru.taximaxim.treeviewer.models;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Класс, выполняющий работу с формированием таблиц и данных.
 * От него необходимо будет унаследоваться и переопределить методы для получения текста колонок
 * getElements должен превращать список данных для treeViewerа;
 * getChildren должен возвращать список дочерних объектов того же типа;
 * getParent должен возвращать родительский объект, если нужно, чаще null;
 * hasChildren должен возвращать true если у объекта есть дочерние объекты;
 * **********************************************************************
 * getColumns возвращает список объектов, где объект колонки имплементит IColumn;
 * getColumnImage получить для строки объекта изображение для определенной колонки
 * getColumnText получить значение для определенной ячейки. сделать метод getRowText(element, getColumns().get(columnIndex))
 * для возможно проходить не по индексу колонки, а по самой колонке
 * localizeString позволяет получить строку из resourceBundle
 */
public abstract class DataSource<T extends IObject> implements ITableLabelProvider, ITreeContentProvider {

    protected List<ILabelProviderListener> listeners = new ArrayList<>();

    public abstract List<? extends IColumn> getColumns();

    public abstract List<? extends IColumn> getColumnsToFilter();

    public abstract ResourceBundle getResourceBundle();

    public abstract String getRowText(Object element, IColumn column);

    public abstract int compare(Object e1, Object e2, IColumn column);

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
