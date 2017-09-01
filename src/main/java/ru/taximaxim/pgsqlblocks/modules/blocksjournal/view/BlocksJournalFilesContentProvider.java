package ru.taximaxim.pgsqlblocks.modules.blocksjournal.view;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.List;

public class BlocksJournalFilesContentProvider implements IStructuredContentProvider {
    @Override
    public Object[] getElements(Object inputElement) {
        return ((List<?>) inputElement).toArray();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }
}
