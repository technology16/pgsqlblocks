package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AddDatabaseDialog extends Dialog {

    protected final Settings settings = Settings.getInstance();
    protected final ResourceBundle resourceBundle = settings.getResourceBundle();

    protected DBModel createdModel;

    protected Text nameText;
    protected Text hostText;
    protected Text portText;
    protected Text userText;
    protected Text passwordText;
    protected Text databaseNameText;
    protected Button enabledButton;

    private static final String DEFAULT_PORT = "5432";
    private static final int TEXT_WIDTH = 200;

    private List<String> reservedConnectionNames;

    public AddDatabaseDialog(Shell shell, List<String> reservedConnectionNames) {
        super(shell);
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
                        resourceBundle.getString("attention"), resourceBundle.getString("use_pgpass_file"));
            }
        });

        Label databaseNameLabel = new Label(container, SWT.HORIZONTAL);
        databaseNameLabel.setText(resourceBundle.getString("database_name"));
        databaseNameText = new Text(container, SWT.BORDER);
        databaseNameText.setLayoutData(textGd);

        Label enabledLabel = new Label(container, SWT.HORIZONTAL);
        enabledLabel.setText(resourceBundle.getString("connect_automatically"));
        enabledButton = new Button(container, SWT.CHECK);

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
        boolean enabled = enabledButton.getSelection();
        if (name.isEmpty()) {
            MessageDialog.openError(null, resourceBundle.getString("attention"),
                    resourceBundle.getString("missing_connection_name"));
            return;
        } else if (reservedConnectionNames.contains(name)) {
            MessageDialog.openError(null, resourceBundle.getString("attention"),
                    MessageFormat.format(resourceBundle.getString("already_exists"), name));
            return;
        } else if (host.isEmpty() || port.isEmpty()) {
            MessageDialog.openError(null, resourceBundle.getString("attention"),
                    resourceBundle.getString("missing_host_port"));
            return;
        } else if (databaseName.isEmpty() || user.isEmpty()) {
            MessageDialog.openError(null, resourceBundle.getString("attention"),
                    resourceBundle.getString("missing_database_user"));
            return;
        }

        createdModel = new DBModel(name, host, port, databaseName, user, password, enabled);

        super.okPressed();
    }

}
