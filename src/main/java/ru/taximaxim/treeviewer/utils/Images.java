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
package ru.taximaxim.treeviewer.utils;

import java.util.ResourceBundle;

public enum Images {

    UPDATE("images/update_16.png", "update"),
    CLEAN("images/clean_16.png", "clean"),
    FILTER("images/filter.png", "filter"),
    TABLE("images/table_16.png", "columns");

    private String location;
    private String description;

    private Images(String location, String description) {
        this.location = location;
        this.description = description;
    }

    public String getImageAddr() {
        return location;
    }

    public String getDescription(ResourceBundle resources) {
        return resources.getString(description);
    }
}
