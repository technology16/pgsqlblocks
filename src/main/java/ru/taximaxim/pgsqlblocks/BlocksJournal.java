package ru.taximaxim.pgsqlblocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.process.Process;

public class BlocksJournal {

    private static final Logger LOG = Logger.getLogger(BlocksJournal.class);

    private final List<BlocksJournalNote> notes;

    public BlocksJournal() {
        notes = new ArrayList<>();
    }

    public void addProcesses(List<Process> processes) {
        if (processes != null && !processes.isEmpty()) {
            notes.add(new BlocksJournalNote(processes));
        }
    }

    public List<BlocksJournalNote> getNotes() {
        return notes;
    }

    public void clear() {
        notes.clear();
    }

}


