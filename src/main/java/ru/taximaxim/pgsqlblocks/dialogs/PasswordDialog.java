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

        String builder = MessageFormat.format("{0} \nName = {1} Database = {2} User = {3}", resourceBundle.getString("type_password_for"),
                model.getName(), model.getDatabaseName(), model.getUser());
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
