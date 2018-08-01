/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
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
package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.common.FilterCondition;
import ru.taximaxim.pgsqlblocks.common.FilterValueType;
import ru.taximaxim.pgsqlblocks.common.models.DBProcessFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DBProcessesFiltersView extends Composite {

    private ResourceBundle resourceBundle;

    private Group group;

    private Combo pidFilterCombo;
    private Text pidFilterText;

    private Combo applicationFilterCombo;
    private Text applicationFilterText;

    private Combo databaseFilterCombo;
    private Text databaseFilterText;

    private Combo userNameFilterCombo;
    private Text userNameFilterText;

    private Combo clientFilterCombo;
    private Text clientFilterText;

    private Combo queryFilterCombo;
    private Text queryFilterText;

    private final List<DBProcessesFiltersViewListener> listeners = new ArrayList<>();

    public DBProcessesFiltersView(ResourceBundle resourceBundle, Composite parent, int style) {
        super(parent, style);
        this.resourceBundle = resourceBundle;
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
        group = new Group(this, SWT.NONE);
        GridLayout layout = new GridLayout(9, false);
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        group.setLayout(layout);
        group.setLayoutData(layoutData);
        group.setText(resourceBundle.getString("process_filter"));

        GridData comboLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false,false);
        comboLayoutData.widthHint = 60;
        GridData textLayoutData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
        textLayoutData.widthHint = 150;
        textLayoutData.minimumWidth = 150;

        createPidFilterView(comboLayoutData, textLayoutData);
        createApplicationFilterView(comboLayoutData, textLayoutData);
        createDatabaseFilterView(comboLayoutData, textLayoutData);
        createUserNameFilterView(comboLayoutData, textLayoutData);
        createClientFilterView(comboLayoutData, textLayoutData);
        createQueryFilterView(comboLayoutData, textLayoutData);
    }

    private void createPidFilterView(GridData comboLayoutData, GridData textLayoutData) {
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(resourceBundle.getString("pid"));

        pidFilterCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        pidFilterCombo.setLayoutData(comboLayoutData);
        fillCombo(pidFilterCombo, FilterValueType.INTEGER);
        pidFilterCombo.select(0);
        pidFilterCombo.addModifyListener(e -> {
            FilterCondition condition = FilterCondition.getFilterConditionFromConditionText(pidFilterCombo.getText());
            listeners.forEach(listener -> listener.processesFiltersViewPidFilterConditionChanged(this, condition));
        });

        pidFilterText = new Text(group, SWT.BORDER);
        pidFilterText.setLayoutData(textLayoutData);
        pidFilterText.addListener(SWT.Verify, new IntegerValueTypeVerifyListener());
        pidFilterText.addModifyListener(e -> {
            String pidFilterTextText = pidFilterText.getText();
            if (pidFilterTextText.startsWith("0") && pidFilterTextText.length() > 1) {
                pidFilterText.setText("0");
                pidFilterText.setSelection(1);
            } else {
                listeners.forEach(listener -> listener.processesFiltersViewPidFilterValueChanged(this, convertTextToInteger(pidFilterText.getText())));
            }
        });
    }

    private void createDatabaseFilterView(GridData comboLayoutData, GridData textLayoutData) {
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(resourceBundle.getString("db_name"));

        databaseFilterCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        databaseFilterCombo.setLayoutData(comboLayoutData);
        fillCombo(databaseFilterCombo, FilterValueType.STRING);
        databaseFilterCombo.select(0);
        databaseFilterCombo.addModifyListener(e -> {
            FilterCondition condition = FilterCondition.getFilterConditionFromConditionText(databaseFilterCombo.getText());
            listeners.forEach(listener -> listener.processesFiltersViewDatabaseFilterConditionChanged(this, condition));
        });

        databaseFilterText = new Text(group, SWT.BORDER);
        databaseFilterText.setLayoutData(textLayoutData);
        databaseFilterText.addModifyListener(e -> {
            String text = databaseFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewDatabaseFilterValueChanged(this, result));
        });
    }

    private void createApplicationFilterView(GridData comboLayoutData, GridData textLayoutData) {
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(resourceBundle.getString("application"));

        applicationFilterCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        applicationFilterCombo.setLayoutData(comboLayoutData);
        fillCombo(applicationFilterCombo, FilterValueType.STRING);
        applicationFilterCombo.select(0);
        applicationFilterCombo.addModifyListener(e -> {
            FilterCondition condition = FilterCondition.getFilterConditionFromConditionText(applicationFilterCombo.getText());
            listeners.forEach(listener -> listener.processesFiltersViewApplicationFilterConditionChanged(this, condition));
        });

        applicationFilterText = new Text(group, SWT.BORDER);
        applicationFilterText.setLayoutData(textLayoutData);
        applicationFilterText.addModifyListener(e -> {
            String text = applicationFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewApplicationFilterValueChanged(this, result));
        });
    }

    private void createQueryFilterView(GridData comboLayoutData, GridData textLayoutData) {
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(resourceBundle.getString("query"));

        queryFilterCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        queryFilterCombo.setLayoutData(comboLayoutData);
        fillCombo(queryFilterCombo, FilterValueType.STRING);
        queryFilterCombo.select(0);
        queryFilterCombo.addModifyListener(e -> {
            FilterCondition condition = FilterCondition.getFilterConditionFromConditionText(queryFilterCombo.getText());
            listeners.forEach(listener -> listener.processesFiltersViewQueryFilterConditionChanged(this, condition));
        });

        queryFilterText = new Text(group, SWT.BORDER);
        queryFilterText.setLayoutData(textLayoutData);
        queryFilterText.addModifyListener(e -> {
            String text = queryFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewQueryFilterValueChanged(this, result));
        });
    }

    private void createUserNameFilterView(GridData comboLayoutData, GridData textLayoutData) {
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(resourceBundle.getString("user_name"));

        userNameFilterCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        userNameFilterCombo.setLayoutData(comboLayoutData);
        fillCombo(userNameFilterCombo, FilterValueType.STRING);
        userNameFilterCombo.select(0);
        userNameFilterCombo.addModifyListener(e -> {
            FilterCondition condition = FilterCondition.getFilterConditionFromConditionText(userNameFilterCombo.getText());
            listeners.forEach(listener -> listener.processesFiltersViewUserNameFilterConditionChanged(this, condition));
        });

        userNameFilterText = new Text(group, SWT.BORDER);
        userNameFilterText.setLayoutData(textLayoutData);
        userNameFilterText.addModifyListener(e -> {
            String text = userNameFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewUserNameFilterValueChanged(this, result));
        });
    }

    private void createClientFilterView(GridData comboLayoutData, GridData textLayoutData) {
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(resourceBundle.getString("client"));

        clientFilterCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        clientFilterCombo.setLayoutData(comboLayoutData);
        fillCombo(clientFilterCombo, FilterValueType.STRING);
        clientFilterCombo.select(0);
        clientFilterCombo.addModifyListener(e -> {
            FilterCondition condition = FilterCondition.getFilterConditionFromConditionText(clientFilterCombo.getText());
            listeners.forEach(listener -> listener.processesFiltersViewClientFilterConditionChanged(this, condition));
        });

        clientFilterText = new Text(group, SWT.BORDER);
        clientFilterText.setLayoutData(textLayoutData);
        clientFilterText.addModifyListener(e -> {
            String text = clientFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewClientFilterValueChanged(this, result));
        });
    }

    private Integer convertTextToInteger(String text) {
        if (text.isEmpty()) {
            return null;
        } else {
            return Integer.valueOf(text);
        }
    }

    private void fillCombo(Combo combo, FilterValueType valueType) {
        List<FilterCondition> conditions = FilterCondition.getConditionsForValueType(valueType);
        conditions.forEach(condition -> combo.add(condition.toString()));
    }

    private class IntegerValueTypeVerifyListener implements Listener {
        @Override
        public void handleEvent(Event event) {
            String string = event.text;
            char[] chars = new char[string.length()];
            string.getChars(0, chars.length, chars, 0);
            for (char aChar : chars) {
                if (!('0' <= aChar && aChar <= '9')) {
                    event.doit = false;
                    return;
                }
            }
        }
    }

    public void fillView(DBProcessFilter filter, String databaseName) {
        if (filter == null) {
            resetFiltersContent();
            return;
        }

        Integer pidFilterValue = filter.getPidFilter().getValue();
        FilterCondition pidFilterCondition = filter.getPidFilter().getCondition();
        pidFilterText.setText(pidFilterValue == null ? "" : String.valueOf(pidFilterValue));
        pidFilterCombo.setText(pidFilterCondition.toString());

        String databaseFilterValue = filter.getDatabaseFilter().getValue();
        FilterCondition databaseFilterCondition = filter.getDatabaseFilter().getCondition();
        if (databaseFilterValue == null && databaseFilterCondition == FilterCondition.NONE) {
            databaseFilterText.setText(databaseName);
        } else {
            databaseFilterText.setText(databaseFilterValue == null ? "" : databaseFilterValue);
        }
        databaseFilterCombo.setText(databaseFilterCondition.toString());

        String clientFilterValue = filter.getClientFilter().getValue();
        FilterCondition clientFilterCondition = filter.getApplicationFilter().getCondition();
        clientFilterText.setText(clientFilterValue == null ? "" : clientFilterValue);
        clientFilterCombo.setText(clientFilterCondition.toString());

        String applicationFilterValue = filter.getApplicationFilter().getValue();
        FilterCondition applicationFilterCondition = filter.getApplicationFilter().getCondition();
        applicationFilterText.setText(applicationFilterValue == null ? "" : applicationFilterValue);
        applicationFilterCombo.setText(applicationFilterCondition.toString());

        String queryFilterValue = filter.getQueryFilter().getValue();
        FilterCondition queryFilterCondition = filter.getQueryFilter().getCondition();
        queryFilterText.setText(queryFilterValue == null ? "" : queryFilterValue);
        queryFilterCombo.setText(queryFilterCondition.toString());

        String userNameFilterValue = filter.getUserNameFilter().getValue();
        FilterCondition userNameFilterCondition = filter.getUserNameFilter().getCondition();
        userNameFilterText.setText(userNameFilterValue == null ? "" : userNameFilterValue);
        userNameFilterCombo.setText(userNameFilterCondition.toString());
    }

    public void resetFiltersContent() {
        pidFilterText.setText("");
        pidFilterCombo.setText("");

        applicationFilterText.setText("");
        applicationFilterCombo.setText("");

        databaseFilterText.setText("");
        databaseFilterCombo.setText("");

        queryFilterCombo.setText("");
        queryFilterText.setText("");

        userNameFilterCombo.setText("");
        userNameFilterText.setText("");

        clientFilterCombo.setText("");
        clientFilterText.setText("");

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

    public void addListener(DBProcessesFiltersViewListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBProcessesFiltersViewListener listener) {
        listeners.remove(listener);
    }
}
