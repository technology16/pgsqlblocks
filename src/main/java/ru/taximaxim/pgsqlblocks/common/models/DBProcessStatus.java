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
package ru.taximaxim.pgsqlblocks.common.models;

import ru.taximaxim.pgsqlblocks.utils.Images;

public enum DBProcessStatus {
    WORKING("Working"),
    BLOCKING("Blocking"),
    BLOCKED("Blocked");

    private final String descr;

    DBProcessStatus(String descr) {
        this.descr = descr;
    }

    public String getDescr() {
        return descr;
    }

    public static DBProcessStatus getInstanceForDescr(String descr) {
        if (descr == null || descr.isEmpty()) {
            return WORKING;
        }

        switch (descr) {
        case "Working":
            return WORKING;
        case "Blocking":
            return BLOCKING;
        case "Blocked":
            return BLOCKED;
        default:
            return WORKING;
        }
    }

    /**
     * Получение иконки в зависимости от состояния
     */
    public Images getStatusImage() {
        switch(this) {
        case BLOCKING:
            return Images.PROC_BLOCKING;
        case BLOCKED:
            return Images.PROC_BLOCKED;
        default:
            return Images.PROC_WORKING;
        }
    }
}
