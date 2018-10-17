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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * GUI of Filters
 */
public class FilterComposite extends Composite {

    private List<? extends IColumn> filterList;
    private ResourceBundle innerResourceBundle;
    private DataSource<? extends IObject> dataSource;
    private int numberOfColumns = 1;
    private FilterChangeHandler filterChangeHandler;
    private List<Text> filterTextList = new ArrayList<>();

    public FilterComposite(Composite parent, int style, ResourceBundle innerResourceBundle,
                           DataSource<? extends IObject> dataSource, FilterChangeHandler filterChangeHandler) {
        super(parent, style);
        this.innerResourceBundle = innerResourceBundle;
        this.dataSource = dataSource;
        this.filterList = dataSource.getColumnsToFilter();
        this.filterChangeHandler = filterChangeHandler;

        numberOfColumns = findColumnNumber() * 3;
        GridLayout glayout = new GridLayout();
        glayout.numColumns = numberOfColumns;
        setLayout(glayout);

        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        setLayoutData(layoutData);

        createContent();
    }

    private void createContent() {
        createAllTextFilter();
        filterList.forEach(this::createFilterView);
    }

    private void createAllTextFilter() {
        Label label = new Label(this, SWT.NONE);
        GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        label.setLayoutData(data);
        label.setText(innerResourceBundle.getString("filter"));
        label.setToolTipText(innerResourceBundle.getString("all-filter-tooltip"));

        Text filterText = new Text(this, SWT.FILL | SWT.BORDER);
        int horizontalSpan = numberOfColumns - 1 > 0 ? numberOfColumns - 1 : 1;
        GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, horizontalSpan, 1);
        filterText.setLayoutData(textLayoutData);
        filterText.setToolTipText(innerResourceBundle.getString("all-filter-tooltip"));
        filterTextList.add(filterText);
        filterText.addModifyListener(e -> filterChangeHandler.filterAllColumns(filterText.getText()));
    }

    private void createFilterView(IColumn column) {
        GridData comboLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false,false);
        comboLayoutData.widthHint = 60;
        GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        textLayoutData.widthHint = 150;

        Label label = new Label(this, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(dataSource.getLocalizeString(column.getColumnName()));

        Combo combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(comboLayoutData);
        List<FilterOperation> filterValues = Arrays.asList(FilterOperation.values());
        filterValues.forEach( f -> combo.add(f.toString()));
        combo.select(FilterOperation.CONTAINS.ordinal());

        Text filterText = new Text(this, SWT.FILL | SWT.BORDER);
        filterText.setLayoutData(textLayoutData);
        filterTextList.add(filterText);

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
        filterChangeHandler.deactivateFilters();
        filterTextList.forEach(t -> t.setText(""));
        this.getParent().layout();
    }
}