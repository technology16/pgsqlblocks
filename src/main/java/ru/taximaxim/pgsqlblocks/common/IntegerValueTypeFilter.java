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

public class IntegerValueTypeFilter extends Filter<Integer> {

    public IntegerValueTypeFilter() {
        super(FilterValueType.INTEGER);
    }

    @Override
    public boolean filter(Integer actualValue) {
        if (!isActive()) {
            return true;
        }
        switch (condition) {
            case NONE:
                return true;
            case EQUALS:
                return actualValue.equals(value);
            case NOT_EQUALS:
                return !actualValue.equals(value);
            case GREATER:
                return actualValue > value;
            case GREATER_OR_EQUAL:
                return actualValue >= value;
            case LESS:
                return actualValue < value;
            case LESS_OR_EQUAL:
                return actualValue <= value;
            case CONTAINS:
                return String.valueOf(actualValue).toLowerCase().contains(String.valueOf(value).toLowerCase());
            default:
                return true;
        }
    }
}
