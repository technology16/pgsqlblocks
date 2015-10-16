package ru.taximaxim.pgsqlblocks;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ProcessTreeContentProvider implements ITreeContentProvider {
    /**
     * Gets the children of the specified object
     * 
     * @param arg0
     *            the parent object
     * @return Object[]
     */
    public Object[] getChildren(Object arg0) {
        return ((Process) arg0).getChildren().toArray();
    }

    /**
     * Gets the parent of the specified object
     * 
     * @param arg0
     *            the object
     * @return Object
     */
    public Object getParent(Object arg0) {
        return ((Process) arg0).getParent();
    }

    /**
     * Returns whether the passed object has children
     * 
     * @param arg0
     *            the parent object
     * @return boolean
     */
    public boolean hasChildren(Object arg0) {
        return ((Process) arg0).hasChildren();
    }

    /**
     * Gets the root element(s) of the tree
     * 
     * @param arg0
     *            the input data
     * @return Object[]
     */
    public Object[] getElements(Object arg0) {
        return ((ProcessTree) arg0).getProcessTree().toArray();
    }

    /**
     * Disposes any created resources
     */
    public void dispose() {
     // TODO Auto-generated method stub
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub
    }
}