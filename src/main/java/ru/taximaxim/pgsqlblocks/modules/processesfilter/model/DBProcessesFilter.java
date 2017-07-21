package ru.taximaxim.pgsqlblocks.modules.processesfilter.model;

import ru.taximaxim.pgsqlblocks.common.models.DBProcess;

import java.util.Arrays;
import java.util.List;

public abstract class DBProcessesFilter<T> {

    protected T value;

    protected DBProcessesFilterCondition selectedCondition = DBProcessesFilterCondition.NONE;

    protected DBProcessesFilterCondition[] supportedConditions = new DBProcessesFilterCondition[]{DBProcessesFilterCondition.NONE};

    public List<DBProcessesFilterCondition> getSupportedConditions() {
        return Arrays.asList(supportedConditions);
    }

    public void setSelectedCondition(DBProcessesFilterCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Filter condition cannot be null");
        }
        if (!getSupportedConditions().contains(condition)) {
            throw new IllegalArgumentException("Filter does not support condition: " + condition);
        }
        this.selectedCondition = condition;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isActive() {
        if (selectedCondition == DBProcessesFilterCondition.NONE) {
            return false;
        }
        if (value == null) {
            return false;
        }
        return true;
    }

    public abstract boolean filter(DBProcess process);

}

