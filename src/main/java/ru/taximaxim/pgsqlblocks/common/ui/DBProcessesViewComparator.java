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
package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;

public class DBProcessesViewComparator extends ViewerComparator {

    private final Columns column;

    private final int sortDirection;

    public DBProcessesViewComparator(Columns column, int sortDirection) {
        this.column = column;
        this.sortDirection = sortDirection;
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int compareResult = 0;
        DBProcess process1 = (DBProcess) e1;
        DBProcess process2 = (DBProcess) e2;
        switch (column) {
            case PID:
                compareResult = compareIntegerValues(process1.getPid(), process2.getPid());
                break;
            case BLOCKED_COUNT:
                compareResult = compareIntegerValues(process1.getChildren().size(), process2.getChildren().size());
                break;
            case APPLICATION_NAME:
                compareResult = compareStringValues(process1.getQueryCaller().getApplicationName(),
                        process2.getQueryCaller().getApplicationName());
                break;
            case DATABASE_NAME:
                compareResult = compareStringValues(process1.getQueryCaller().getDatabaseName(),
                        process2.getQueryCaller().getDatabaseName());
                break;
            case USER_NAME:
                compareResult = compareStringValues(process1.getQueryCaller().getUserName(),
                        process2.getQueryCaller().getUserName());
                break;
            case CLIENT:
                compareResult = compareStringValues(process1.getQueryCaller().getClient(),
                        process2.getQueryCaller().getClient());
                break;
            case BACKEND_START:
                compareResult = DateUtils.compareDates(process1.getQuery().getBackendStart(),
                        process2.getQuery().getBackendStart());
                break;
            case QUERY_START:
                compareResult = DateUtils.compareDates(process1.getQuery().getQueryStart(),
                        process2.getQuery().getQueryStart());
                break;
            case XACT_START:
                compareResult = DateUtils.compareDates(process1.getQuery().getXactStart(),
                        process2.getQuery().getXactStart());
                break;
            case DURATION:
                compareResult = DateUtils.compareDurations(process1.getQuery().getDuration(),
                        process2.getQuery().getDuration());
                break;
            case STATE:
                compareResult = compareStringValues(process1.getState(), process2.getState());
                break;
            case STATE_CHANGE:
                compareResult = DateUtils.compareDates(process1.getStateChange(), process2.getStateChange());
                break;
            case BLOCKED:
                compareResult = compareStringValues(process1.getBlocksPidsString(), process2.getBlocksPidsString());
                break;
            case LOCK_TYPE:
                compareResult = compareStringValues(process1.getBlocksLocktypesString(),
                        process2.getBlocksLocktypesString());
                break;
            case RELATION:
                compareResult = compareStringValues(process1.getBlocksRelationsString(),
                        process2.getBlocksRelationsString());
                break;
            case QUERY:
                compareResult = compareStringValues(process1.getQuery().getQueryString(),
                        process2.getQuery().getQueryString());
                break;
            case SLOW_QUERY:
                compareResult = compareBooleans(process1.getQuery().isSlowQuery(), process2.getQuery().isSlowQuery());
                break;
            default:
                break;

        }
        return sortDirection == SWT.DOWN ? compareResult : -compareResult;
    }

    private int compareIntegerValues(int i1, int i2) {
        Integer integer1 = i1;
        Integer integer2 = i2;
        return integer1.compareTo(integer2);
    }

    private int compareStringValues(String s1, String s2) {
        return s1.compareTo(s2);
    }

    private int compareBooleans(boolean b1, boolean b2) {
        return b1 == b2 ? 0 : b1 ? 1 : -1;
    }
}
