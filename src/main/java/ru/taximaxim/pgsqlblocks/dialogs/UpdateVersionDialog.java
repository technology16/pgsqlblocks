package ru.taximaxim.pgsqlblocks.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
    protected Control createDialogArea(Composite parent) {
        return super.createDialogArea(parent);
    }

}
