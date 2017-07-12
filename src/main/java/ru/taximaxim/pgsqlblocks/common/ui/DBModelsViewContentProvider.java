package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.List;

public class DBModelsViewContentProvider implements IStructuredContentProvider {

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
