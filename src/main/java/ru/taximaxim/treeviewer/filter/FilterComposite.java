package ru.taximaxim.treeviewer.filter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.IObject;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * GUI of Filters
 */
public class FilterComposite extends Composite {

    private GridLayout glayout;
    private List<? extends IColumn> filterList;
    private ResourceBundle innerResourceBundle;
    private DataSource<? extends IObject> dataSource;
    private int numberOfColumns = 1;
    private FilterChangeHandler filterChangeHandler;

    public FilterComposite(Composite parent, int style, ResourceBundle innerResourceBundle,
                           DataSource<? extends IObject> dataSource, FilterChangeHandler filterChangeHandler) {
        super(parent, style);
        this.innerResourceBundle = innerResourceBundle;
        this.dataSource = dataSource;
        glayout = new GridLayout();
        glayout.marginWidth = 0;
        glayout.marginHeight = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        setLayout(glayout);
        setLayoutData(layoutData);
        this.filterList = dataSource.getColumnsToFilter();
        this.filterChangeHandler = filterChangeHandler;

        createContent();
    }

    private void createContent() {
        numberOfColumns = findColumnNumber();
        glayout.numColumns = numberOfColumns;
        createAllTextFilter();
        filterList.forEach(this::createFilterView);
    }

    private void createAllTextFilter() {
        Composite group = new Composite(this, SWT.NULL);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = numberOfColumns;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, numberOfColumns, 1 );
        group.setLayout(layout);
        group.setLayoutData(layoutData);

        Label label = new Label(group, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
        label.setLayoutData(data);
        label.setText(innerResourceBundle.getString("filter"));

        Text filterText = new Text(group, SWT.FILL | SWT.BORDER);
        GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        filterText.setLayoutData(textLayoutData);
        filterText.addModifyListener(e -> filterChangeHandler.filterAllColumns(filterText.getText()));
    }

    private void createFilterView(IColumn column) {
        Composite group = new Composite(this, SWT.NULL);
        GridLayout layout = new GridLayout(3, false);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        group.setLayout(layout);
        group.setLayoutData(layoutData);

        GridData comboLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false,false);
        comboLayoutData.widthHint = 60;
        GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        textLayoutData.widthHint = 150;

        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(dataSource.getLocalizeString(column.getColumnName()));

        Combo combo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(comboLayoutData);
        List<FilterOperation> filterValues = Arrays.asList(FilterOperation.values());
        filterValues.forEach( f -> combo.add(f.toString()));
        combo.select(FilterOperation.CONTAINS.ordinal());

        Text filterText = new Text(group, SWT.FILL | SWT.BORDER);
        filterText.setLayoutData(textLayoutData);

        combo.addModifyListener(e -> filterChangeHandler.filter(filterText.getText(), FilterOperation.find(combo.getText()), column));
        filterText.addModifyListener(e -> filterChangeHandler.filter(filterText.getText(), FilterOperation.find(combo.getText()), column));
    }

    /**
     * Method returns the number of columns in filter
     */
    private int findColumnNumber() {
        int i = filterList.size();
        if (i == 1) {
            return 1;
        }else if (i==4 || i == 2) {
            return 2;
        } else {
            return 3;
        }
    }

    public void show() {
        this.setVisible(true);
        GridData layoutData = (GridData) this.getLayoutData();
        layoutData.exclude = false;
        this.getParent().layout();
    }

    public void hide() {
        this.setVisible(false);
        GridData layoutData = (GridData) this.getLayoutData();
        layoutData.exclude = true;
        this.getParent().layout();
    }
}