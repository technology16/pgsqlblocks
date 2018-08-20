package test;

import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Created by user on 16.08.18.
 */
public class Col implements IColumn {

    private final String columnName;
    private final String columnTooltip;
    private final int columnWidth;

    public Col(String columnName, String columnTooltip, int columnWidth) {
        this.columnName = columnName;
        this.columnTooltip = columnTooltip;
        this.columnWidth = columnWidth;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public String getColumnTooltip() {
        return columnTooltip;
    }

    @Override
    public int getColumnWidth() {
        return 80;
    }
}
