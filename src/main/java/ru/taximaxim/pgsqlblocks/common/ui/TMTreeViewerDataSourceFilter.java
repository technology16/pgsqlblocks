package ru.taximaxim.pgsqlblocks.common.ui;

public interface TMTreeViewerDataSourceFilter<T> {

    boolean filter(T object);

}
