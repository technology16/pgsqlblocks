package ru.taximaxim.treeviewer.models;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс выполняющий работу с формированием таблиц и данных.
 * От него необходимо будет унаследоваться и переопределить методы для получения текста колонок
 * getElements должен превращать список данных для treeViewerа;
 * getChildren должен возвращать список дочерних объектов того же типа;
 * getParent должен возвращать родительский объект, если нужно, чаще null;
 * hasChildren должен возвращать true если у объекта есть дочерние объекты;
 * **********************************************************************
 * getColumns возвращает список объектов, где объект колонки имплементит Icolumn;
 * columnIsSortable если содержимое колонки может сортироваться
 * getColumnImage получить для строки объекта изображение для определенной колонки
 * getColumnText получить значение для определенной ячейки. сделать метод getRowText(element, getColumns().get(columnIndex))
 * для возможно проходить не по индексу колонки, а по самой колонке
 * localizeString позволяет получить строку из resourceBundle
 */
public abstract class MyTreeViewerDataSource implements ITableLabelProvider, ITreeContentProvider {

    protected List<ILabelProviderListener> listeners = new ArrayList<>();

    public MyTreeViewerDataSource() {
    }

    public abstract boolean columnIsSortable();

    public abstract List<? extends IColumn> getColumns();

    public abstract String getLocalizeString(String name);

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
