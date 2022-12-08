/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017-2022 "Technology" LLC
 * %
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
package ru.taximaxim.pgsqlblocks.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Dialog for password for database
 */
public class PasswordDialog extends Dialog {
    private ResourceBundle resourceBundle;
    private String password;
    private Text passwordText;
    private DBModel model;

    public PasswordDialog(ResourceBundle resourceBundle, Shell shell, DBModel model) {
        super(shell);
        this.resourceBundle = resourceBundle;
        this.model = model;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(resourceBundle.getString("attention"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(1, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        layout.marginTop = 10;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        String builder = MessageFormat.format("{0} {1}\n {2}:{3}/{4}?user={5}", resourceBundle.getString("type_password_for"),
                model.getName(), model.getHost(), model.getPort(), model.getDatabaseName(), model.getUser());
        Label answerLabel = new Label(container, SWT.HORIZONTAL);
        answerLabel.setText(builder);
        passwordText = new Text(container, SWT.BORDER);
        passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        passwordText.setEchoChar('•');
        return container;
    }

    @Override
    protected void okPressed() {
        password = passwordText.getText();
        super.okPressed();
    }

    public String getPassword() {
        return password;
    }
}
