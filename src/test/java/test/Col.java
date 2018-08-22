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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Col col = (Col) o;

        if (columnWidth != col.columnWidth) return false;
        if (columnName != null ? !columnName.equals(col.columnName) : col.columnName != null) return false;
        return columnTooltip != null ? columnTooltip.equals(col.columnTooltip) : col.columnTooltip == null;
    }

    @Override
    public int hashCode() {
        int result = columnName != null ? columnName.hashCode() : 0;
        result = 31 * result + (columnTooltip != null ? columnTooltip.hashCode() : 0);
        result = 31 * result + columnWidth;
        return result;
    }
}
