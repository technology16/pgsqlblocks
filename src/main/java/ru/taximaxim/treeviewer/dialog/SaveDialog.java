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
package ru.taximaxim.treeviewer.dialog;

import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;
import ru.taximaxim.pgsqlblocks.utils.Images;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.xmlstore.DBBlocksXmlStore;

public class SaveDialog extends Dialog {

    private final ResourceBundle bundle;
    private final DBController controller;
    private Text path;

    public SaveDialog(ResourceBundle bundle, DBController controller, Shell parent) {
        super(parent);
        this.bundle = bundle;
        this.controller = controller;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(bundle.getString("save_all"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        c.setLayout(layout);
        path = new Text(c, SWT.BORDER);

        path.setText(PathBuilder.getInstance().getBlocksJournalsDir().toString());
        path.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        Button btnDir = new Button(c, SWT.PUSH);
        btnDir.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true));
        btnDir.setImage(ImageUtils.getImage(Images.FOLDER));
        btnDir.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                dialog.setText(bundle.getString("choose_dir"));
                dialog.setFilterPath(PathBuilder.getInstance().getBlocksJournalsDir().toString());
                String p = dialog.open();
                if (p != null) {
                    path.setText(p);
                }
            }
        });
        return c;
    }

    @Override
    protected void okPressed() {
        List<DBBlocksJournalProcess> list = controller.getBlocksJournal().getProcesses();
        String p = path.getText();
        String fileName = p.substring(p.lastIndexOf('/') + 1);
        if (!fileName.endsWith(".xml")) {
            MessageBox m = new MessageBox(getShell());
            m.setText("wrong file name");
            m.setMessage("wrong file name \'" + fileName + "\'. file name must end by \'.xml\'");
            m.open();
        } else {
            DBBlocksXmlStore store = new DBBlocksXmlStore(p);
            store.writeObjects(list);
            super.okPressed();
        }
    }
}