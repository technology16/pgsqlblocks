package ru.taximaxim.pgsqlblocks.modules.processesfilter.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class DBProcessesFiltersView extends Composite {

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

    }

}
