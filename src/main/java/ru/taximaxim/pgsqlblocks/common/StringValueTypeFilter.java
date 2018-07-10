/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
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
package ru.taximaxim.pgsqlblocks.common;

public class StringValueTypeFilter extends Filter<String> {

    public StringValueTypeFilter() {
        super(FilterValueType.STRING);
    }

    @Override
    public boolean filter(String actualValue) {
        if (!isActive()) {
            return true;
        }
        switch (condition) {
            case NONE:
                return true;
            case EQUALS:
                return actualValue.equals(value);
            case CONTAINS:
                return actualValue.toLowerCase().contains(value.toLowerCase());
            default:
                return true;
        }
    }

    @Override
    public boolean isActive() {
        return super.isActive() && !value.isEmpty();
    }
}
