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

public class DBProcessesFiltersView extends Composite {

    private Combo pidFilterCombo;
    private Text pidFilterText;

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
        GridLayout layout = new GridLayout(3, false);
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        group.setLayout(layout);
        group.setLayoutData(layoutData);
        group.setText("Filters");

        GridData labelLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        GridData comboLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false,false);
        comboLayoutData.widthHint = 60;
        GridData textLayoutData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
        textLayoutData.widthHint = 150;

        Label pidFilterLabel = new Label(group, SWT.NONE);
        pidFilterLabel.setLayoutData(labelLayoutData);
        pidFilterLabel.setText("PID");

        pidFilterCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        pidFilterCombo.setLayoutData(comboLayoutData);
        fillCombo(pidFilterCombo, FilterValueType.INTEGER);
        pidFilterCombo.select(0);
        pidFilterCombo.addModifyListener(e -> {
            FilterCondition condition = FilterCondition.getFilterConditionFromConditionText(pidFilterCombo.getText());
            listeners.forEach(listener -> listener.pidFilterConditionChanged(condition));
        });

        pidFilterText = new Text(group, SWT.NONE);
        pidFilterText.setLayoutData(textLayoutData);
        pidFilterText.addListener(SWT.Verify, new IntegerValueTypeVerifyListener());
        pidFilterText.addModifyListener(e -> {
                String pidFilterTextText = pidFilterText.getText();
                if (pidFilterTextText.startsWith("0")) {
                    pidFilterText.setText("");
                } else {
                    listeners.forEach(listener -> listener.pidFilterValueChanged(convertTextToInteger(pidFilterText.getText())));
                }
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
        } else {
            pidFilterText.setText("");
            pidFilterCombo.setText("");
        }
    }

    public void addListener(DBProcessesFiltersViewListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBProcessesFiltersViewListener listener) {
        listeners.remove(listener);
    }

}
