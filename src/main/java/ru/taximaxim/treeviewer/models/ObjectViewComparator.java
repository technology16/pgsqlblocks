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

import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Класс необходим для сортировки колонки по возрастанию/убыванию
 * setColumn должен получает колонку treeColumn.getData()
 * setSortDirection получает тип сортировки.
 */
public abstract class ObjectViewComparator extends ViewerComparator {

    public ObjectViewComparator() {
        super();
    }

    public abstract void setColumn(TreeColumn column);

    public abstract void setSortDirection(int sortDirection);

    public abstract int compareIntegerValues(int one, int two);

    public abstract int compareStringValues(String one, String two);

    public abstract int compareBooleans(boolean one, boolean two);
}
