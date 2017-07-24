package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.common.FilterCondition;
import ru.taximaxim.pgsqlblocks.common.FilterValueType;
import ru.taximaxim.pgsqlblocks.common.models.DBProcessFilter;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DBProcessesFiltersView extends Composite {

    private Settings settings = Settings.getInstance();
    private ResourceBundle resourceBundle = settings.getResourceBundle();

    private Combo pidFilterCombo;
    private Text pidFilterText;

    private Combo applicationFilterCombo;
    private Text applicationFilterText;

    private Combo queryFilterCombo;
    private Text queryFilterText;

    private final List<DBProcessesFiltersViewListener> listeners = new ArrayList<>();

    public DBProcessesFiltersView(Composite parent, int style) {
        super(parent, style);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
        Group group = new Group(this, SWT.NONE);
        GridLayout layout = new GridLayout(9, false);
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        group.setLayout(layout);
        group.setLayoutData(layoutData);
        group.setText("Filters");

        GridData comboLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false,false);
        comboLayoutData.widthHint = 60;
        GridData textLayoutData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
        textLayoutData.widthHint = 150;
        textLayoutData.minimumWidth = 150;

        Label pidFilterLabel = new Label(group, SWT.NONE);
        pidFilterLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        pidFilterLabel.setText(resourceBundle.getString("pid"));

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

        Label applicationFilterLabel = new Label(group, SWT.NONE);
        applicationFilterLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        applicationFilterLabel.setText(resourceBundle.getString("application"));

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
            String queryFilterTextText = applicationFilterText.getText();
            final String result = queryFilterTextText.isEmpty() ? null : queryFilterTextText;
            listeners.forEach(listener -> listener.processesFiltersViewApplicationFilterValueChanged(result));
        });

        Label queryFilterLabel = new Label(group, SWT.NONE);
        queryFilterLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        queryFilterLabel.setText(resourceBundle.getString("query"));

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
            String queryFilterTextText = queryFilterText.getText();
            final String result = queryFilterTextText.isEmpty() ? null : queryFilterTextText;
            listeners.forEach(listener -> listener.processesFiltersViewQueryFilterValueChanged(result));
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

    public void fillViewWithData(DBProcessFilter filter) {
        if (filter != null) {
            Integer pidFilterValue = filter.getPidFilter().getValue();
            FilterCondition pidFilterCondition = filter.getPidFilter().getCondition();
            pidFilterText.setText(pidFilterValue == null ? "" : String.valueOf(pidFilterValue));
            pidFilterCombo.setText(pidFilterCondition.toString());

            String queryFilterValue = filter.getQueryFilter().getValue();
            FilterCondition queryFilterCondition = filter.getQueryFilter().getCondition();
            queryFilterText.setText(queryFilterValue == null ? "" : queryFilterValue);
            queryFilterCombo.setText(queryFilterCondition.toString());

        } else {
            pidFilterText.setText("");
            pidFilterCombo.setText("");

            applicationFilterText.setText("");
            applicationFilterCombo.setText("");

            queryFilterCombo.setText("");
            queryFilterText.setText("");
        }
    }

    public void addListener(DBProcessesFiltersViewListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBProcessesFiltersViewListener listener) {
        listeners.remove(listener);
    }

}
