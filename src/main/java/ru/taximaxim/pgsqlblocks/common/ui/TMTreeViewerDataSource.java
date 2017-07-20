package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class TMTreeViewerDataSource implements ITableLabelProvider, ITreeContentProvider {

    protected TMTreeViewerDataSourceFilter dataFilter;

    public TMTreeViewerDataSource() {

    }

    public TMTreeViewerDataSource(TMTreeViewerDataSourceFilter dataFilter) {
        this.dataFilter = dataFilter;
    }

    public void setDataFilter(TMTreeViewerDataSourceFilter dataFilter) {
        this.dataFilter = dataFilter;
    }

    protected final ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<>();

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


    protected Image getImage(String path) {
        return imagesMap.computeIfAbsent(path, k ->
                new Image(null, getClass().getClassLoader().getResourceAsStream(path)));
    }

}
