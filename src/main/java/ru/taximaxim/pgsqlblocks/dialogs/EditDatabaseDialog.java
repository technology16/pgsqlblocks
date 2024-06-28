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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.taximaxim.pgsqlblocks.common.models.DBModel;

public class EditDatabaseDialog extends AddDatabaseDialog {

    private final DBModel editedModel;

    public EditDatabaseDialog(ResourceBundle resourceBundle, Shell shell,
            List<String> reservedConnectionNames, DBModel editedModel) {
        super(resourceBundle, shell, reservedConnectionNames);
        reservedConnectionNames.remove(editedModel.getName());
        this.editedModel = editedModel;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(resourceBundle.getString("edit_connection"));
    }

    public DBModel getEditedModel() {
        return editedModel;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control dialogArea = super.createDialogArea(parent);
        nameText.setText(editedModel.getName());
        hostText.setText(editedModel.getHost());
        portText.setText(editedModel.getPort());
        databaseNameText.setText(editedModel.getDatabaseName());
        userText.setText(editedModel.getUser());
        passwordText.setText(editedModel.getPassword());
        readBackendTypeButton.setSelection(editedModel.isReadBackendType());
        enabledButton.setSelection(editedModel.isEnabled());
        return dialogArea;
    }
}
