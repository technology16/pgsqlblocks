package ru.taximaxim.pgsqlblocks.dbcdata;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DbcDataListContentProvider implements IStructuredContentProvider {

    /**
     * Gets the root element(s) of the tree
     */
    @Override
    public Object[] getElements(Object arg0) {
        return ((List<?>) arg0).toArray();
    }

    /**
     * Disposes any created resources
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when the input changes
     */
    @Override
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
        // TODO Auto-generated method stub
    }
}