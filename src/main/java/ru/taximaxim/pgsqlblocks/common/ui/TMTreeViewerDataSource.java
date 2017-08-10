package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class TMTreeViewerDataSource<T> implements ITableLabelProvider, ITreeContentProvider {

    protected TMTreeViewerDataSourceFilter<T> dataFilter;

    public TMTreeViewerDataSource() {

    }

    public TMTreeViewerDataSource(TMTreeViewerDataSourceFilter dataFilter) {
        this.dataFilter = dataFilter;
    }

    public void setDataFilter(TMTreeViewerDataSourceFilter dataFilter) {
        this.dataFilter = dataFilter;
    }

    protected List<ILabelProviderListener> listeners = new ArrayList<>();

    public abstract int numberOfColumns();

    public abstract String columnTitleForColumnIndex(int columnIndex);

    public abstract int columnWidthForColumnIndex(int columnIndex);

    public abstract boolean columnIsSortableAtIndex(int columnIndex);

    public abstract String columnTooltipForColumnIndex(int columnIndex);

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
