/*
 * Copyright 2017 "Technology" LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;

import java.util.List;

public class AddDbcDataDlg extends Dialog {

    private static final String DEFAULT_PORT = "5432";
    private static final int TEXT_WIDTH = 200;
    public static final String ATTENTION_WORD = "Внимание!";

    private final DbcData editedDbcData;
    private final List<DbcData> dbcDataList;
    private DbcData newDbcData;

    private Text nameText;
    private Text hostText;
    private Text portText;
    private Text userText;
    private Text passwdText;
    private Text dbnameText;
    private Button enabledButton;

    public DbcData getNewDbcData() {
        return newDbcData;
    }

    public DbcData getEditedDbcData() {
        return editedDbcData;
    }
    
    public AddDbcDataDlg(Shell shell, DbcData dbcData, List<DbcData> dbcDataList) {
        super(shell);
        this.editedDbcData = dbcData;
        this.dbcDataList = dbcDataList;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      
      GridLayout layout = new GridLayout(2, false);
      layout.marginRight = 5;
      layout.marginLeft = 10;
      container.setLayout(layout);

      GridData textGd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
      textGd.widthHint = TEXT_WIDTH;
      
      Label nameLabel = new Label(container, SWT.HORIZONTAL);
      nameLabel.setText("Имя соединения*");
      nameText = new Text(container, SWT.BORDER);
      nameText.setLayoutData(textGd);
      
      Label hostLabel = new Label(container, SWT.HORIZONTAL);
      hostLabel.setText("Хост*");
      hostText = new Text(container, SWT.BORDER);
      hostText.setLayoutData(textGd);
      
      Label portLabel = new Label(container, SWT.HORIZONTAL);
      portLabel.setText("Порт*");
      portText = new Text(container, SWT.BORDER);
      portText.setText(DEFAULT_PORT);
      portText.setLayoutData(textGd);
      
      Label userLabel = new Label(container, SWT.HORIZONTAL);
      userLabel.setText("Имя пользователя*");
      userText = new Text(container, SWT.BORDER);
      userText.setLayoutData(textGd);
      
      Label passwdLabel = new Label(container, SWT.HORIZONTAL);
      passwdLabel.setText("Пароль");
      passwdText = new Text(container, SWT.BORDER);
      passwdText.setLayoutData(textGd);
      passwdText.setEchoChar('•');
      passwdText.addListener(SWT.FocusOut, event -> {
          if (!passwdText.getText().isEmpty()) {
              MessageDialog.openWarning(null,
                      ATTENTION_WORD, "Указание пароля здесь небезопасно. Используйте .pgpass файл.");
          }
      });
      
      Label dbnameLabel = new Label(container, SWT.HORIZONTAL);
      dbnameLabel.setText("Имя БД*");
      dbnameText = new Text(container, SWT.BORDER);
      dbnameText.setLayoutData(textGd);
      
      Label enabledLabel = new Label(container, SWT.HORIZONTAL);
      enabledLabel.setText("Подкл. автоматически");
      enabledButton = new Button(container, SWT.CHECK);
      
      if (editedDbcData != null) {
          nameText.setText(editedDbcData.getName());
          hostText.setText(editedDbcData.getHost());
          portText.setText(editedDbcData.getPort());
          dbnameText.setText(editedDbcData.getDbname());
          userText.setText(editedDbcData.getUser());
          passwdText.setText(editedDbcData.getPass());
          enabledButton.setSelection(editedDbcData.isEnabledAutoConnect());
      }

      return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      if (editedDbcData == null) {
          newShell.setText("Добавить новое соединение");
      } else {
          newShell.setText("Редактировать соединение");
      }
    }

    @Override
    protected Point getInitialSize() {
        return new Point(410, 330);
    }
    
    @Override
    protected void okPressed() {
        String name = nameText.getText();
        String host = hostText.getText();
        String port = portText.getText();
        String dbname = dbnameText.getText();
        String user = userText.getText();
        String passwd = passwdText.getText();
        boolean enabled = enabledButton.getSelection();
        if (name.isEmpty()) {
            MessageDialog.openError(null, ATTENTION_WORD, "Не заполнено обязательное поле: Имя соединения!");
            return;
        } else if (editedDbcData != null && !editedDbcData.getName().equals(name) && dbcDataList.stream().anyMatch(d -> d.getName().equals(name))) {
            MessageDialog.openError(null, ATTENTION_WORD, "Сервер с таким именем существует!");
            return;
        } else if (host.isEmpty() || port.isEmpty()) {
            MessageDialog.openError(null, ATTENTION_WORD, "Не заполнены обязательные поля: Хост и/или Порт!");
            return;
        } else if (dbname.isEmpty() || user.isEmpty()) {
            MessageDialog.openError(null, ATTENTION_WORD, "Не заполнены обязательные поля: Имя БД и/или Имя пользователя!");
            return;
        }

        newDbcData = new DbcData(name, host, port, dbname, user, passwd, enabled);

        super.okPressed();
    }
}
