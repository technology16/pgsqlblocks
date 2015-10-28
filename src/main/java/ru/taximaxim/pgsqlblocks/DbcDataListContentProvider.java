package ru.taximaxim.pgsqlblocks;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DbcDataListContentProvider implements IStructuredContentProvider {

    /**
     * Gets the root element(s) of the tree
     * 
     * @param arg0
     *            the input data
     * @return Object[]
     */
    public Object[] getElements(Object arg0) {
        return ((List<DbcData>) arg0).toArray();
    }

    /**
     * Disposes any created resources
     */
    public void dispose() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when the input changes
     * 
     * @param arg0
     *            the viewer
     * @param arg1
     *            the old input
     * @param arg2
     *            the new input
     */
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
        // TODO Auto-generated method stub
    }
}