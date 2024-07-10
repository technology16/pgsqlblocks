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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class DBBlocksJournalTest {

    private final static String INCORRECT_NUMB_OF_PROC = "Incorrect number of processes in DBBlocksJournal";

    static class Listener implements DBBlocksJournalListener {

        private boolean isDidCloseProcesses;
        private boolean isDidCloseAllProcesses;
        private boolean isDidAddProcesses;

        @Override
        public void dbBlocksJournalDidAddProcesses() {
            isDidAddProcesses = true;
        }

        @Override
        public void dbBlocksJournalDidCloseAllProcesses() {
            isDidCloseAllProcesses = true;
        }

        @Override
        public void dbBlocksJournalDidCloseProcesses(List<DBBlocksJournalProcess> processes) {
            isDidCloseProcesses = true;
        }

        public boolean verifyDidCloseProcesses() {
            if (isDidCloseProcesses) {
                isDidCloseProcesses = false;
                return true;
            }
            return false;
        }

        public boolean verifyDidCloseAllProcesses() {
            if (isDidCloseAllProcesses) {
                isDidCloseAllProcesses = false;
                return true;
            }
            return false;
        }

        public boolean verifyDidAddProcesses() {
            if (isDidAddProcesses) {
                isDidAddProcesses = false;
                return true;
            }
            return false;
        }
    }

    @Test
    public void addProcessesTest() {
        DBBlocksJournal journal = new DBBlocksJournal();
        Listener listener = new Listener();
        journal.addListener(listener);

        List<DBProcess> empty = Collections.emptyList();
        journal.add(empty);
        assertTrue("DBBlocksJournal is not empty", journal.isEmpty());

        DBProcessQuery procQuery = createDBProcessQuery();
        DBProcess proc1 = createDBProcess(0, procQuery);
        DBProcess proc2 = createDBProcess(1, procQuery);
        proc1.addChild(proc2);
        List<DBProcess> procList = new ArrayList<>();
        procList.add(proc1);
        procList.add(proc2);
        journal.add(procList);
        assertFalse("DBBlocksJournal is empty", journal.isEmpty());
        assertEquals(INCORRECT_NUMB_OF_PROC, 2, journal.getProcesses().size());

        List<DBProcess> procList2 = new ArrayList<>();
        DBProcess proc3 = createDBProcess(3, procQuery);
        procList2.add(proc3);
        journal.add(procList2);
        assertEquals(INCORRECT_NUMB_OF_PROC, 3, journal.getProcesses().size());
        assertTrue("Error in method add() variable needCloseProcesses is empty", listener.verifyDidCloseProcesses());

        journal.add(procList2);
        assertEquals(INCORRECT_NUMB_OF_PROC, 3, journal.getProcesses().size());

        journal.getProcesses().get(2).close();
        journal.add(procList2);
        assertTrue("Wrong status of processes must be closed.", journal.getProcesses().get(2).isClosed());
        assertEquals(INCORRECT_NUMB_OF_PROC, 4, journal.getProcesses().size());

        journal.add(empty);
        assertTrue(
                "Error while processing method closeProcesses didn't enter into method dbBlocksJournalDidCloseProcesses",
                listener.verifyDidCloseProcesses());
        assertTrue(
                "Error while processing method closeProcesses didn't enter into method dbBlocksJournalDidCloseAllProcesses",
                listener.verifyDidCloseAllProcesses());
    }

    @Test
    public void setJournalProcessesTest() {
        DBBlocksJournal journal = new DBBlocksJournal();
        Listener listener = new Listener();
        journal.addListener(listener);

        List<DBBlocksJournalProcess> processes = new ArrayList<>();
        DBProcessQuery procQuery = createDBProcessQuery();
        DBBlocksJournalProcess journalProc = new DBBlocksJournalProcess(createDBProcess(0, procQuery));
        processes.add(journalProc);

        journal.setJournalProcesses(processes);
        assertTrue(
                "Error while processing method setJournalProcesses didn't enter into method dbBlocksJournalDidAddProcesses",
                listener.verifyDidAddProcesses());
    }

    private DBProcessQuery createDBProcessQuery() {
        return new DBProcessQuery("query", false, new Date(), new Date(), new Date(), "15678");
    }

    private DBProcess createDBProcess(int pid, DBProcessQuery processQuery) {
        DBProcessQueryCaller c = new DBProcessQueryCaller("appName", "test", "user", "client");
        return new DBProcess(pid, "type", c, "state", new Date(), processQuery);
    }
}