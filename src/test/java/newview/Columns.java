package newview;

import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Created by user on 23.08.18.
 */
public enum Columns implements IColumn{
    NAME("name", "NAME", 100),
    TITLE("title", "TITLE", 100),
    AUTHOR("author", "AUTHOR", 100),
    PRICE("price", "PRICE", 100);

    private final String columnName;
    private final String columnTooltip;
    private final int columnWidth;

    Columns(String columnName, String columnTooltip, int columnWidth) {
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
        return columnWidth;
    }
}
