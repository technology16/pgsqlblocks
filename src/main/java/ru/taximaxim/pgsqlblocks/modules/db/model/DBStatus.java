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
package ru.taximaxim.pgsqlblocks.modules.db.model;

import ru.taximaxim.pgsqlblocks.utils.Images;

public enum DBStatus {
    DISABLED,
    CONNECTED,
    CONNECTION_ERROR,
    UPDATE;

    /**
     * Получение иконки в зависимости от состояния
     */
    public Images getStatusImage() {
        switch(this) {
        case DISABLED:
            return Images.CONN_DISABLED;
        case CONNECTED:
            return Images.CONN_CONNECTED;
        case CONNECTION_ERROR:
            return Images.CONN_ERROR;
        case UPDATE:
            return Images.CONN_UPDATE;
        default:
            throw new IllegalStateException("Unsupported status: " + this);
        }
    }
}
