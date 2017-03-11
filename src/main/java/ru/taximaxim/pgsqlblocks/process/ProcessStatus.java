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
package ru.taximaxim.pgsqlblocks.process;

import ru.taximaxim.pgsqlblocks.utils.Images;

public enum ProcessStatus {
    WORKING,
    BLOCKING,
    BLOCKED;
    
    /**
     * Получение иконки в зависимости от состояния
     * @return
     */
    public Images getStatusImage() {
        switch(this) {
        case WORKING:
            return Images.PROC_WORKING;
        case BLOCKING:
            return Images.PROC_BLOCKING;
        case BLOCKED:
            return Images.PROC_BLOCKED;
        default:
            return Images.DEFAULT;
        }
    }
}
