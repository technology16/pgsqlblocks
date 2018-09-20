package ru.taximaxim.pgsqlblocks.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by 11 on 19.09.2018.
 */
public class UpdateVersionDialog extends Dialog {

    private ResourceBundle resourceBundle;
    private List<String> modelList;

    public UpdateVersionDialog(ResourceBundle resourceBundle, Shell parentShell, List<String> modelList) {
        super(parentShell);
        this.resourceBundle = resourceBundle;
        this.modelList = modelList;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(resourceBundle.getString("warning_title"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(1, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        layout.marginTop = 10;
        container.setLayout(layout);

        Label warningLabel = new Label(container, SWT.CENTER);
        warningLabel.setText(resourceBundle.getString("warning_text"));
        modelList.forEach(model -> createLabelArea(container, model));
        return container;
    }

    private void createLabelArea(Composite container, String text) {
        Label connectionLabel = new Label(container, SWT.LEFT | SWT.FILL);
        connectionLabel.setText(text);
    }


}
