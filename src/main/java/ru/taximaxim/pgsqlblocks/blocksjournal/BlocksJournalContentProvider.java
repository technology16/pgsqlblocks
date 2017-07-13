package ru.taximaxim.pgsqlblocks.blocksjournal;

import org.eclipse.jface.viewers.ITreeContentProvider;
import ru.taximaxim.pgsqlblocks.process.Process;

import java.util.List;

public class BlocksJournalContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
            List<BlocksJournalProcess> elements = (List<BlocksJournalProcess>) inputElement;
            return elements.toArray();
        }
        if (inputElement instanceof BlocksJournalProcess) {
            BlocksJournalProcess process = (BlocksJournalProcess) inputElement;
            return process.getProcess().getChildren().toArray();
        }
        if (inputElement instanceof Process) {
            Process process = (Process) inputElement;
            return process.getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof BlocksJournalProcess) {
            BlocksJournalProcess process = (BlocksJournalProcess) parentElement;
            return process.getProcess().getChildren().toArray();
        }
        if (parentElement instanceof Process) {
            Process process = (Process) parentElement;
            return process.getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof BlocksJournalProcess) {
            return true;
        }
        if (element instanceof Process) {
            Process process = (Process) element;
            return process.hasChildren();
        }
        return false;
    }
}
