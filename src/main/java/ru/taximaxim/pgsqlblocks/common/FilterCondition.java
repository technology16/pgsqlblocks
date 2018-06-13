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
package ru.taximaxim.pgsqlblocks.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum FilterCondition {

    NONE(""),
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    LESS("<"),
    LESS_OR_EQUAL("<="),
    CONTAINS("~");

    private final String conditionText;

    FilterCondition(String conditionText) {
        this.conditionText = conditionText;
    }

    @Override
    public String toString() {
        return conditionText;
    }

    public static List<FilterCondition> getConditionsForValueType(FilterValueType valueType) {
        List<FilterCondition> result = new ArrayList<>();
        result.add(NONE);
        switch (valueType) {
            case INTEGER:
                result.add(CONTAINS);
                result.add(EQUALS);
                result.add(NOT_EQUALS);
                result.add(GREATER);
                result.add(GREATER_OR_EQUAL);
                result.add(LESS);
                result.add(LESS_OR_EQUAL);
                break;
            case STRING:
                result.add(CONTAINS);
                result.add(EQUALS);
                break;
        }
        return result;
    }

    public static FilterCondition getFilterConditionFromConditionText(String conditionText) {
        Optional<FilterCondition> filterCondition = Arrays.stream(FilterCondition.values())
                .filter(fc -> fc.conditionText.equals(conditionText))
                .findFirst();
        return filterCondition.orElse(FilterCondition.NONE);
    }
}
