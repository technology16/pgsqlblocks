package ru.taximaxim.pgsqlblocks.common.ui;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;

import java.util.List;

public class EditDatabaseDialog extends AddDatabaseDialog {

    private DBModel editedModel;

    public EditDatabaseDialog(Shell shell, List<String> reservedConnectionNames, DBModel editedModel) {
        super(shell, reservedConnectionNames);
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
        enabledButton.setSelection(editedModel.isEnabled());
        return dialogArea;
    }
}
