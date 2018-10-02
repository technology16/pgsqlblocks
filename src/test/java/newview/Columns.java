package newview;

import ru.taximaxim.treeviewer.utils.ColumnType;
import ru.taximaxim.treeviewer.models.IColumn;

/**
 * Created by user on 23.08.18.
 */
public enum Columns implements IColumn{
    NAME("name", "NAME", 100, ColumnType.STRING),
    TITLE("title", "TITLE", 100, ColumnType.STRING),
    AUTHOR("author", "AUTHOR", 100, ColumnType.STRING),
    PRICE("price", "PRICE", 100, ColumnType.INTEGER);

    private final String columnName;
    private final String columnTooltip;
    private final int columnWidth;
    private final ColumnType columnType;

    Columns(String columnName, String columnTooltip, int columnWidth, ColumnType columnType) {
        this.columnName = columnName;
        this.columnTooltip = columnTooltip;
        this.columnWidth = columnWidth;
        this.columnType = columnType;
    }

    @Override
    public ColumnType getColumnType(){
        return columnType;
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
