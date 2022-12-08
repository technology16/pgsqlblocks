/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017-2022 "Technology" LLC
 * %
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.treeviewer.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.treeviewer.models.DataSource;

/**
 * GUI of Filters
 */
public class FilterComposite extends Composite {

    private final Set<Columns> filterList;
    private final ResourceBundle innerResourceBundle;
    private final DataSource<?> dataSource;
    private final int numberOfColumns;
    private final FilterChangeHandler filterChangeHandler;
    private final List<Text> filterTextList = new ArrayList<>();

    public FilterComposite(Composite parent, int style, ResourceBundle innerResourceBundle,
            DataSource<?> dataSource, FilterChangeHandler filterChangeHandler) {
        super(parent, style);
        this.innerResourceBundle = innerResourceBundle;
        this.dataSource = dataSource;
        this.filterList = dataSource.getColumnsToFilter();
        this.filterChangeHandler = filterChangeHandler;

        // label, combo, text
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
        int horizontalSpan = Math.max(numberOfColumns - 1, 1);
        GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, horizontalSpan, 1);
        filterText.setLayoutData(textLayoutData);
        filterText.setToolTipText(innerResourceBundle.getString("all-filter-tooltip"));
        filterTextList.add(filterText);
        filterText.addModifyListener(e -> filterChangeHandler.filterAllColumns(filterText.getText()));
    }

    private void createFilterView(Columns column) {
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

        combo.addModifyListener(e -> filterChangeHandler.filter(
                filterText.getText(), FilterOperation.find(combo.getText()), column));
        filterText.addModifyListener(e -> filterChangeHandler.filter(
                filterText.getText(), FilterOperation.find(combo.getText()), column));
    }

    /**
     * Method returns the number of columns in filter
     */
    private int findColumnNumber() {
        int i = filterList.size();

        if (i == 4) {
            return 2;
        }

        return Math.min(i, 3);
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
