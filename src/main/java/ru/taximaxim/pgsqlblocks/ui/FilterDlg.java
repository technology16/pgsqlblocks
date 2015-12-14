package ru.taximaxim.pgsqlblocks.ui;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.taximaxim.pgsqlblocks.utils.FilterProcess;

public class FilterDlg extends Dialog {
    
    private static final Logger LOG = Logger.getLogger(FilterDlg.class);
    
    private static final int TEXT_WIDTH = 200;

    private Text pidText;
    private Text dbNameText;
    private Text userNameText;
    private Text backendStartText;
    private Text queryStartText;
    private Combo pidCombo;
    private Combo dbNameCombo;
    private Combo userNameCombo;
    private Combo backendStartCombo;
    private Combo queryStartCombo;
    private FilterProcess filterProcess;
    
    public FilterDlg(Shell shell, FilterProcess filterProcess) {
        super(shell);
        this.filterProcess = filterProcess;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      GridLayout layout = new GridLayout(3, false);
      layout.marginRight = 5;
      layout.marginLeft = 10;
      container.setLayout(layout);

      GridData textGd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
      textGd.widthHint = TEXT_WIDTH;
      
      Label pidLabel = new Label(container, SWT.HORIZONTAL);
      pidLabel.setText("pid");
      pidCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      fillOperList(pidCombo);
      pidText = new Text(container, SWT.BORDER);
      pidText.setLayoutData(textGd);
      pidText.addListener(SWT.Verify, new Listener() {
          public void handleEvent(Event e) {
              String string = e.text;
              char[] chars = new char[string.length()];
              string.getChars(0, chars.length, chars, 0);
              for (int i = 0; i < chars.length; i++) {
                  if (!('0' <= chars[i] && chars[i] <= '9')) {
                      e.doit = false;
                      return;
                  }
              }
          }
      });

      Label dbNameLabel = new Label(container, SWT.HORIZONTAL);
      dbNameLabel.setText("datname");
      dbNameCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      fillOperListString(dbNameCombo);
      dbNameText = new Text(container, SWT.BORDER);
      dbNameText.setLayoutData(textGd);
      
      Label userNameLabel = new Label(container, SWT.HORIZONTAL);
      userNameLabel.setText("username");
      userNameCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      fillOperListString(userNameCombo);
      userNameText = new Text(container, SWT.BORDER);
      userNameText.setLayoutData(textGd);
      
      Label backendStartLabel = new Label(container, SWT.HORIZONTAL);
      backendStartLabel.setText("backend_start");
      backendStartCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      fillOperList(backendStartCombo);
      backendStartText = new Text(container, SWT.BORDER);
      backendStartText.setLayoutData(textGd);

              
      Label queryStartLabel = new Label(container, SWT.HORIZONTAL);
      queryStartLabel.setText("query_start");
      queryStartCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      fillOperList(queryStartCombo);
      queryStartText = new Text(container, SWT.BORDER);
      queryStartText.setLayoutData(textGd);

      pidText.setText(filterProcess.getPid().getValue());
      dbNameText.setText(filterProcess.getDbName().getValue());
      userNameText.setText(filterProcess.getUserName().getValue());
      backendStartText.setText(filterProcess.getBackendStart().getValue());
      queryStartText.setText(filterProcess.getQueryStart().getValue());
      
      pidCombo.setText(filterProcess.getPid().getOperation());
      dbNameCombo.setText(filterProcess.getDbName().getOperation());
      userNameCombo.setText(filterProcess.getUserName().getOperation());
      backendStartCombo.setText(filterProcess.getBackendStart().getOperation());
      queryStartCombo.setText(filterProcess.getQueryStart().getOperation());

      return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Фильтр процессов");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(410, 250);
    }
    
    @Override
    protected void okPressed() {
        
        filterProcess.getPid().setValue(pidText.getText());
        filterProcess.getDbName().setValue(dbNameText.getText());
        filterProcess.getUserName().setValue(userNameText.getText());
        filterProcess.getBackendStart().setValue(backendStartText.getText());
        filterProcess.getQueryStart().setValue(queryStartText.getText());
        
        filterProcess.getPid().setOperation(pidCombo.getText());
        filterProcess.getDbName().setOperation(dbNameCombo.getText());
        filterProcess.getUserName().setOperation(userNameCombo.getText());
        filterProcess.getBackendStart().setOperation(backendStartCombo.getText());
        filterProcess.getQueryStart().setOperation(queryStartCombo.getText());
        
        super.okPressed();
    }
    
    private void fillOperList(Combo combo) {
        combo.add("=");
        combo.add("!=");
        combo.add(">");
        combo.add(">=");
        combo.add("<");
        combo.add("<=");
    }
    
    private void fillOperListString(Combo combo) {
        combo.add("=");
        combo.add("!=");
    }
}
