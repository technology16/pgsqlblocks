package ru.taximaxim.pgsqlblocks.modules.logs.view;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import ru.taximaxim.pgsqlblocks.ui.UIAppender;
import ru.taximaxim.pgsqlblocks.utils.Settings;

public class LogsView extends Composite {

    private final Settings settings;

    public LogsView(Composite parent, Settings settings, int style) {
        super(parent, style);
        this.settings = settings;
        GridLayout layout = new GridLayout();
        layout.marginTop = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
        UIAppender uiAppender = new UIAppender(this, settings.getLocale());
        uiAppender.setThreshold(Level.INFO);
        Logger.getRootLogger().addAppender(uiAppender);
    }
}
