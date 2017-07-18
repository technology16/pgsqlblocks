package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class TMTreeViewerDataSource implements ITableLabelProvider, ITreeContentProvider {

    protected List<ILabelProviderListener> listeners = new ArrayList<>();

    abstract int numberOfColumns();

    abstract String columnTitleForColumnIndex(int columnIndex);

    abstract int columnWidthForColumnIndex(int columnIndex);

    abstract String columnTooltipForColumnIndex(int columnIndex);

    @Override
    public void dispose() {

    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }
}
