/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.dialogs;

import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.taximaxim.pgsqlblocks.common.models.DBModel;

public class AddDatabaseDialog extends Dialog {

    private static final String ATTENTION = "attention";

    protected final ResourceBundle resourceBundle;

    private DBModel createdModel;

    Text nameText;
    Text hostText;
    Text portText;
    Text userText;
    Text passwordText;
    Text databaseNameText;
    Button readBackendTypeButton;
    Button enabledButton;

    private static final String DEFAULT_PORT = "5432";
    private static final int TEXT_WIDTH = 200;

    private final List<String> reservedConnectionNames;

    public AddDatabaseDialog(ResourceBundle resourceBundle, Shell shell, List<String> reservedConnectionNames) {
        super(shell);
        this.resourceBundle = resourceBundle;
        this.reservedConnectionNames = reservedConnectionNames;
    }

    public DBModel getCreatedModel() {
        return createdModel;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(resourceBundle.getString("add_new_connection"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        layout.marginTop = 10;
        container.setLayout(layout);

        GridData textGd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        textGd.widthHint = TEXT_WIDTH;

        Label nameLabel = new Label(container, SWT.HORIZONTAL);
        nameLabel.setText(resourceBundle.getString("name"));
        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(textGd);

        Label hostLabel = new Label(container, SWT.HORIZONTAL);
        hostLabel.setText(resourceBundle.getString("host"));
        hostText = new Text(container, SWT.BORDER);
        hostText.setLayoutData(textGd);

        Label portLabel = new Label(container, SWT.HORIZONTAL);
        portLabel.setText(resourceBundle.getString("port"));
        portText = new Text(container, SWT.BORDER);
        portText.setText(DEFAULT_PORT);
        portText.setLayoutData(textGd);

        Label userLabel = new Label(container, SWT.HORIZONTAL);
        userLabel.setText(resourceBundle.getString("user"));
        userText = new Text(container, SWT.BORDER);
        userText.setLayoutData(textGd);

        Label passwordLabel = new Label(container, SWT.HORIZONTAL);
        passwordLabel.setText(resourceBundle.getString("password"));
        passwordText = new Text(container, SWT.BORDER);
        passwordText.setLayoutData(textGd);
        passwordText.setEchoChar('•');
        passwordText.addListener(SWT.FocusOut, event -> {
            if (!passwordText.getText().isEmpty()) {
                MessageDialog.openWarning(null,
                        resourceBundle.getString(ATTENTION), resourceBundle.getString("use_pgpass_file"));
            }
        });

        Label databaseNameLabel = new Label(container, SWT.HORIZONTAL);
        databaseNameLabel.setText(resourceBundle.getString("database_name"));
        databaseNameText = new Text(container, SWT.BORDER);
        databaseNameText.setLayoutData(textGd);

        GridData checkGd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);

        readBackendTypeButton = new Button(container, SWT.CHECK);
        readBackendTypeButton.setText(resourceBundle.getString("read_backend_type"));
        readBackendTypeButton.setLayoutData(checkGd);

        enabledButton = new Button(container, SWT.CHECK);
        enabledButton.setText(resourceBundle.getString("connect_automatically"));
        enabledButton.setLayoutData(checkGd);

        return container;
    }

    @Override
    protected void okPressed() {
        String name = nameText.getText();
        String host = hostText.getText();
        String port = portText.getText();
        String databaseName = databaseNameText.getText();
        String user = userText.getText();
        String password = passwordText.getText();
        boolean readBackendType = readBackendTypeButton.getSelection();
        boolean enabled = enabledButton.getSelection();
        if (name.isEmpty()) {
            displayError("missing_connection_name");
            return;
        } else if (reservedConnectionNames.contains(name)) {
            displayError("already_exists", name);
            return;
        } else if (host.isEmpty() || port.isEmpty()) {
            displayError("missing_host_port");
            return;
        } else if (databaseName.isEmpty() || user.isEmpty()) {
            displayError("missing_database_user");
            return;
        }

        createdModel = new DBModel(name, host, port, databaseName, user,
                password, readBackendType, enabled);

        super.okPressed();
    }

    private void displayError(String msg, String... args) {
        MessageDialog.openError(null, resourceBundle.getString(ATTENTION),
                String.format(resourceBundle.getString(msg), (Object[]) args));
    }
}
