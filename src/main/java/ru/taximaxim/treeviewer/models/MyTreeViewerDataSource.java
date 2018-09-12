package ru.taximaxim.treeviewer.models;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import ru.taximaxim.treeviewer.filter.FilterValues;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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

    public String getLocalizeString(String name) {
        return getResourceBundle() == null ? name : getResourceBundle().getString(name);
    }

    public abstract ResourceBundle getResourceBundle();

    public abstract String getRowText(Object element, IColumn column);

    public boolean resolveTypeOfComparator(FilterValues value, String objectValue, String searchValue){
        switch (value){
            case EQUALS:
                return objectValue.equals(searchValue);
            case CONTAINS:
                return objectValue.toLowerCase().contains(searchValue.toLowerCase());
            case NOT_EQUALS:
                return !objectValue.equals(searchValue);
            case LESS:
                return getInteger(objectValue) < getInteger(searchValue);
            case GREATER:
                return getInteger(objectValue) > getInteger(searchValue);
            case LESS_OR_EQUAL:
                return getInteger(objectValue) <= getInteger(searchValue);
            case GREATER_OR_EQUAL:
                return getInteger(objectValue) >= getInteger(searchValue);
            case NONE:
                return true;
            default:
                    return true;
        }
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        return getRowText(element, getColumns().get(columnIndex));
    }

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

    private Integer getInteger(String value){
        Integer i;
        try {
            i = Integer.parseInt(value);
        }catch (NumberFormatException e){
            i = 1;
        }
        return i;
    }
}
