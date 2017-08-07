package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.common.FilterCondition;
import ru.taximaxim.pgsqlblocks.common.FilterValueType;
import ru.taximaxim.pgsqlblocks.common.models.DBProcessFilter;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.utils.Settings;

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

    private Button includeBlockedButton;

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
        createIncludeBlockedProcessesView();
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
            listeners.forEach(listener -> listener.processesFiltersViewPidFilterConditionChanged(condition));
        });

        pidFilterText = new Text(group, SWT.NONE);
        pidFilterText.setLayoutData(textLayoutData);
        pidFilterText.addListener(SWT.Verify, new IntegerValueTypeVerifyListener());
        pidFilterText.addModifyListener(e -> {
            String pidFilterTextText = pidFilterText.getText();
            if (pidFilterTextText.startsWith("0") && pidFilterTextText.length() > 1) {
                pidFilterText.setText("0");
                pidFilterText.setSelection(1);
            } else {
                listeners.forEach(listener -> listener.processesFiltersViewPidFilterValueChanged(convertTextToInteger(pidFilterText.getText())));
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
            listeners.forEach(listener -> listener.processesFiltersViewDatabaseFilterConditionChanged(condition));
        });

        databaseFilterText = new Text(group, SWT.NONE);
        databaseFilterText.setLayoutData(textLayoutData);
        databaseFilterText.addModifyListener(e -> {
            String text = databaseFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewDatabaseFilterValueChanged(result));
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
            listeners.forEach(listener -> listener.processesFiltersViewApplicationFilterConditionChanged(condition));
        });

        applicationFilterText = new Text(group, SWT.NONE);
        applicationFilterText.setLayoutData(textLayoutData);
        applicationFilterText.addModifyListener(e -> {
            String text = applicationFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewApplicationFilterValueChanged(result));
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
            listeners.forEach(listener -> listener.processesFiltersViewQueryFilterConditionChanged(condition));
        });

        queryFilterText = new Text(group, SWT.NONE);
        queryFilterText.setLayoutData(textLayoutData);
        queryFilterText.addModifyListener(e -> {
            String text = queryFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewQueryFilterValueChanged(result));
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
            listeners.forEach(listener -> listener.processesFiltersViewUserNameFilterConditionChanged(condition));
        });

        userNameFilterText = new Text(group, SWT.NONE);
        userNameFilterText.setLayoutData(textLayoutData);
        userNameFilterText.addModifyListener(e -> {
            String text = userNameFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewUserNameFilterValueChanged(result));
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
            listeners.forEach(listener -> listener.processesFiltersViewClientFilterConditionChanged(condition));
        });

        clientFilterText = new Text(group, SWT.NONE);
        clientFilterText.setLayoutData(textLayoutData);
        clientFilterText.addModifyListener(e -> {
            String text = clientFilterText.getText();
            final String result = text.isEmpty() ? null : text;
            listeners.forEach(listener -> listener.processesFiltersViewClientFilterValueChanged(result));
        });
    }

    private void createIncludeBlockedProcessesView() {
        Composite composite = new Composite(group, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false, 9, 1);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(layoutData);

        includeBlockedButton = new Button(composite, SWT.CHECK);
        includeBlockedButton.setText(resourceBundle.getString("include_blocked_processes"));
        includeBlockedButton.addListener(SWT.Selection, e -> {
            listeners.forEach(listener -> listener.processesFiltersViewIncludeBlockedValueChanged(includeBlockedButton.getSelection()));
        });
    }

    private Integer convertTextToInteger(String text) {
        if (text.isEmpty())
            return null;
        Integer result = Integer.valueOf(text);
        return result;
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
            for (int i = 0; i < chars.length; i++) {
                if (!('0' <= chars[i] && chars[i] <= '9')) {
                    event.doit = false;
                    return;
                }
            }
        }
    }

    public void fillViewForController(DBController controller) {
        if (controller == null) {
            resetFiltersContent();
            return;
        }

        DBProcessFilter filter = controller.getProcessesFilters();

        Integer pidFilterValue = filter.getPidFilter().getValue();
        FilterCondition pidFilterCondition = filter.getPidFilter().getCondition();
        pidFilterText.setText(pidFilterValue == null ? "" : String.valueOf(pidFilterValue));
        pidFilterCombo.setText(pidFilterCondition.toString());

        String databaseFilterValue = filter.getDatabaseFilter().getValue();
        FilterCondition databaseFilterCondition = filter.getDatabaseFilter().getCondition();
        if (databaseFilterValue == null && databaseFilterCondition == FilterCondition.NONE) {
            databaseFilterText.setText(controller.getModel().getDatabaseName());
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

        includeBlockedButton.setSelection(filter.isIncludeBlockedProcessesWhenFiltering());
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

        includeBlockedButton.setSelection(false);
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
