/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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
