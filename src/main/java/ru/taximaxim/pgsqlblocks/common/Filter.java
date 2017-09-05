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
import java.util.List;

public abstract class Filter<T> {

    protected final List<FilterListener> listeners = new ArrayList<>();

    protected T value;

    private final List<FilterCondition> supportedConditions;

    private final FilterValueType valueType;

    private boolean isActive = false;

    Filter(FilterValueType valueType) {
        this.valueType = valueType;
        this.supportedConditions = FilterCondition.getConditionsForValueType(this.valueType);
    }

    FilterCondition condition = FilterCondition.CONTAINS;

    private List<FilterCondition> getSupportedConditions() {
        return supportedConditions;
    }

    public void setCondition(FilterCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Filter condition cannot be null");
        }
        if (this.condition == condition) {
            return;
        }
        if (!getSupportedConditions().contains(condition)) {
            throw new IllegalArgumentException("Filter does not support condition: " + condition);
        }
        boolean isActiveBeforeSetCondition = isActive();
        this.condition = condition;
        boolean isActiveAfterSetCondition = isActive();
        if (isActiveBeforeSetCondition == isActiveAfterSetCondition) {
            listeners.forEach(listener -> listener.filterConditionChanged(this));
        }
        setActive(isActiveAfterSetCondition);
    }

    public T getValue() {
        return value;
    }

    public FilterCondition getCondition() {
        return condition;
    }

    public void setValue(T value) {
        if ((this.value == null && value == null) || (this.value != null && this.value.equals(value))) {
            return;
        }
        boolean isActiveBeforeSetValue = isActive();
        this.value = value;
        boolean isActiveAfterSetValue = isActive();
        if (isActiveBeforeSetValue == isActiveAfterSetValue) {
            listeners.forEach(listener -> listener.filterValueChanged(this));
        }
        setActive(isActiveAfterSetValue);
    }

    private void setActive(boolean isActive) {
        if (this.isActive != isActive) {
            this.isActive = isActive;
            if (this.isActive) {
                listeners.forEach(listener -> listener.filterDidActivate(this));
            }
            else {
                listeners.forEach(listener -> listener.filterDidDeactivate(this));
            }
        }
        this.isActive = isActive;
    }

    public abstract boolean filter(T actualValue);

    public void reset() {
        condition = FilterCondition.NONE;
        value = null;
    }

    public boolean isActive() {
        return condition != FilterCondition.NONE && value != null;
    }

    public void addListener(FilterListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FilterListener listener) {
        listeners.remove(listener);
    }
}
