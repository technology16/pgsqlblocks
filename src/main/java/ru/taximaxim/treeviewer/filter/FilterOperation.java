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
package ru.taximaxim.treeviewer.filter;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import ru.taximaxim.treeviewer.utils.TriFunction;

/**
 * Enum for types of column filters
 */
enum FilterOperation {

    NONE("", (o, s, t) -> true),
    EQUALS("=", FilterOperation::equalsByType),
    NOT_EQUALS("!=", (o, s, t) -> !EQUALS.matchesForType(o, s, t)),
    CONTAINS("~", (o, s, t) -> o.toLowerCase().contains(s.toLowerCase())),
    GREATER(">", FilterOperation::greater),
    GREATER_OR_EQUAL(">=", (o, s, t) -> EQUALS.matchesForType(o, s, t) || GREATER.matchesForType(o, s, t)),
    LESS("<", FilterOperation::less),
    LESS_OR_EQUAL("<=", (o, s, t) -> EQUALS.matchesForType(o, s, t) || LESS.matchesForType(o, s, t));

    private final String conditionText;
    private final TriFunction<String, String, ColumnType, Boolean> matcherFunction;

    FilterOperation(String conditionText, TriFunction<String, String, ColumnType, Boolean> matcherFunction) {
        this.conditionText = conditionText;
        this.matcherFunction = matcherFunction;
    }

    boolean matchesForType(String objectValue, String searchValue, ColumnType columnType) {
        return matcherFunction.apply(objectValue, searchValue, columnType);
    }

    @Override
    public String toString() {
        return conditionText;
    }

    public String getConditionText() {
        return conditionText;
    }

    public static FilterOperation find(String text) {
        FilterOperation[] list = FilterOperation.values();
        for (FilterOperation filterOperation : list) {
            if (filterOperation.getConditionText().equals(text)) {
                return filterOperation;
            }
        }
        return FilterOperation.NONE;
    }

    private static boolean equalsByType(String objectValue, String searchValue, ColumnType columnType) {
        switch (columnType) {
        case INTEGER:
            return matches(objectValue, searchValue, FilterOperation::getInteger, Integer::equals);
        case DATE:
        case STRING:
            return objectValue.equals(searchValue);
        }
        return false;
    }

    private static boolean greater(String objectValue, String searchValue, ColumnType columnType) {
        switch (columnType) {
        case INTEGER:
            return matches(objectValue, searchValue, FilterOperation::getInteger, (i1, i2) -> i1 > i2);
        case DATE:
        case STRING:
            return objectValue.compareTo(searchValue) > 0;
        }
        return false;
    }

    private static boolean less(String objectValue, String searchValue, ColumnType columnType) {
        switch (columnType) {
        case INTEGER:
            return matches(objectValue, searchValue, FilterOperation::getInteger, (i1, i2) -> i1 < i2);
        case DATE:
        case STRING:
            return objectValue.compareTo(searchValue) < 0;
        }
        return false;
    }

    private static <T> boolean matches(String objectValue, String searchValue,
            Function<String, Optional<T>> converter, BiFunction<T, T, Boolean> matcher) {
        Optional<T> objectOpt = converter.apply(objectValue);
        Optional<T> searchOpt = converter.apply(searchValue);
        return objectOpt.isPresent() && searchOpt.isPresent() && matcher.apply(objectOpt.get(), searchOpt.get());
    }

    private static Optional<Integer> getInteger(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
