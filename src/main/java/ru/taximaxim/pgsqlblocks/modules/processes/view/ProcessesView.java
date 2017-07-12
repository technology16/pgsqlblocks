package ru.taximaxim.pgsqlblocks.modules.processes.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ProcessesView extends Composite {

    private Composite leftPanelComposite;
    private Composite rightPanelComposite;

    public ProcessesView(Composite parentComposite, int style) {
        super(parentComposite, style);
        GridLayout layout = new GridLayout();
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
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
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
        leftPanelComposite = new Composite(sashForm, SWT.NONE);
        leftPanelComposite.setLayout(layout);
        leftPanelComposite.setLayoutData(layoutData);
    }

    private void createRightPanel(SashForm sashForm) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
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
