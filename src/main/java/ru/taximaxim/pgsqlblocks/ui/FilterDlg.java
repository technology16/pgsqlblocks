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
package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.utils.FilterProcess;

public class FilterDlg extends Dialog {

    private static final int TEXT_WIDTH = 200;

    private Text pidText;
    private Text dbNameText;
    private Text userNameText;
    private Combo backendStartText;
    private Combo queryStartText;
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
      pidText.addListener(SWT.Verify, e -> {
          String string = e.text;
          char[] chars = new char[string.length()];
          string.getChars(0, chars.length, chars, 0);
          for (int i = 0; i < chars.length; i++) {
              if (!('0' <= chars[i] && chars[i] <= '9')) {
                  e.doit = false;
                  return;
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
      backendStartText = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      backendStartText.add("");
      backendStartText.setLayoutData(textGd);
      backendStartText.addListener(SWT.FOCUSED, arg0 -> {
          DateTimeSelectDlg tt = new DateTimeSelectDlg(getShell(), filterProcess, filterProcess.getBackendStart().getKey());
          tt.open();
          backendStartText.removeAll();
          backendStartText.add(filterProcess.getBackendStart().getValue());
          backendStartText.select(0);
      });

      Label queryStartLabel = new Label(container, SWT.HORIZONTAL);
      queryStartLabel.setText("query_start");
      queryStartCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      fillOperList(queryStartCombo);
      queryStartText = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      queryStartText.add("");
      queryStartText.setLayoutData(textGd);
      queryStartText.addListener(SWT.FOCUSED, arg0 -> {
          DateTimeSelectDlg tt = new DateTimeSelectDlg(getShell(), filterProcess, filterProcess.getQueryStart().getKey());
          tt.open();
          queryStartText.removeAll();
          queryStartText.add(filterProcess.getQueryStart().getValue());
          queryStartText.select(0);
      });

      pidText.setText(filterProcess.getPid().getValue());
      dbNameText.setText(filterProcess.getDbName().getValue());
      userNameText.setText(filterProcess.getUserName().getValue());
      backendStartText.removeAll();
      backendStartText.add(filterProcess.getBackendStart().getValue());
      backendStartText.select(0);
      
      queryStartText.removeAll();
      queryStartText.add(filterProcess.getQueryStart().getValue());
      queryStartText.select(0);
      
      pidCombo.setText(filterProcess.getPid().getOperation());
      dbNameCombo.setText(filterProcess.getDbName().getOperation());
      userNameCombo.setText(filterProcess.getUserName().getOperation());
      backendStartCombo.setText(filterProcess.getBackendStart().getOperation());
      queryStartCombo.setText(filterProcess.getQueryStart().getOperation());

      container.pack();
      return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Фильтр процессов");
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
        combo.add("");
        combo.add("=");
        combo.add("!=");
        combo.add(">");
        combo.add(">=");
        combo.add("<");
        combo.add("<=");
    }

    private void fillOperListString(Combo combo) {
        combo.add("");
        combo.add("=");
        combo.add("!=");
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.NO_ID, "Reset", false);
        super.createButtonsForButtonBar(parent);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.CANCEL_ID) {
            filterProcess.filterReset();
            backendStartText.removeAll();
            backendStartText.add("");
            backendStartText.select(0);
            queryStartText.removeAll();
            queryStartText.add("");
            queryStartText.select(0);
        }
        if (buttonId == IDialogConstants.NO_ID) {
            filterProcess.filterReset();
            pidText.setText("");
            dbNameText.setText("");
            userNameText.setText("");
            backendStartText.removeAll();
            backendStartText.add("");
            backendStartText.select(0);
            queryStartText.removeAll();
            queryStartText.add("");
            queryStartText.select(0);
            pidCombo.setText("");
            dbNameCombo.setText("");
            userNameCombo.setText("");
            backendStartCombo.setText("");
            queryStartCombo.setText("");
        } else {
            super.buttonPressed(buttonId);
        }
    }
}
