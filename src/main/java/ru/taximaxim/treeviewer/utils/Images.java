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

    UPDATE,
    FILTER,
    TABLE,
    DEFAULT;

    Images() {
    }

    public String getImageAddr() {
        switch(this) {
            case UPDATE:
                return "images/update_16.png";
            case FILTER:
                return "images/filter.png";
            case TABLE:
                return "images/table_16.png";
            default:
                return "images/void_16.png";
        }
    }

    public String getDescription(ResourceBundle resources) {
        switch(this) {
            case UPDATE:
                return resources.getString("update");
            case FILTER:
                return resources.getString("filter");
            case TABLE:
                return resources.getString("columns");
            default:
                return resources.getString("default_action");
        }
    }
}
