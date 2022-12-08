/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017-2022 "Technology" LLC
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
package ru.taximaxim.pgsqlblocks.utils;

public class ColumnLayout {

    private final int order;
    private final Columns column;
    private final Integer width;

    public ColumnLayout(int order, Columns column, Integer width) {
        this.order = order;
        this.column = column;
        this.width = width;
    }

    public int getOrder() {
        return order;
    }

    public Columns getColumn() {
        return column;
    }

    public Integer getWidth() {
        return width;
    }
}
