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
package ru.taximaxim.pgsqlblocks.modules.processes.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

public class ProcessesView extends Composite {

    private Composite leftPanelComposite;
    private Composite rightPanelComposite;

    private ToolBar toolBar;

    public ProcessesView(Composite parentComposite, int style) {
        super(parentComposite, style);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    private void createContent() {
        toolBar = new ToolBar(this, SWT.HORIZONTAL);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
        sashForm.SASH_WIDTH = 2;
        sashForm.setLayoutData(layoutData);
        sashForm.setLayout(layout);

        createLeftPanel(sashForm);
        createRightPanel(sashForm);

        sashForm.setSashWidth(2);
        sashForm.setWeights(new int[] {15, 85});
    }

    private void createLeftPanel(SashForm sashForm) {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginLeft = 5;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        leftPanelComposite = new Composite(sashForm, SWT.NONE);
        leftPanelComposite.setLayout(layout);
        leftPanelComposite.setLayoutData(layoutData);
    }

    private void createRightPanel(SashForm sashForm) {
        GridLayout layout = new GridLayout();
        layout.marginLeft = 0;
        layout.marginHeight = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        rightPanelComposite = new Composite(sashForm, SWT.NONE);
        rightPanelComposite.setLayout(layout);
        rightPanelComposite.setLayoutData(layoutData);
    }

    public Composite getLeftPanelComposite() {
        return leftPanelComposite;
    }

    public Composite getRightPanelComposite() {
        return rightPanelComposite;
    }
}
