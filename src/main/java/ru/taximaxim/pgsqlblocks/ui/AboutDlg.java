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
package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class AboutDlg extends Dialog {

    private static final String DIALOGTITLE = "О программе";
    private static final String DISTRIB_LINK = "http://pgcodekeeper.org/pgsqlblocks/";
    private static final String HOMEPAGE = "http://pgcodekeeper.org/pgsqlblocks.html";
    private static final String TELEGRAM_LINK = "https://telegram.me/joinchat/Bxn1Zwh02WM96O-55GAryA";
    private static final int OK_BUTTON_HEIGHT_IN_CHARS = 15;

    public AboutDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    public void open() {
        Shell shell = new Shell(getParent(), getStyle());
        shell.setText(DIALOGTITLE);
        createContent(shell);
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    private void createContent(Shell container){

        GridLayout layout = new GridLayout(1, false);
        container.setLayout(layout);

        Image logo = new Image(null,
                getClass().getClassLoader().getResourceAsStream("images/block-48x48.png"));
        Label logoLabel = new Label(container, SWT.HORIZONTAL);
        logoLabel.setImage(logo);

        //FIXME localization???
        Label infoLabel = new Label(container, SWT.HORIZONTAL);
        infoLabel.setText("pgSqlBlocks - это приложение, \n"
                + "которое позволяет легко ориентироваться среди процессов \n"
                + "и получать информацию о блокировках и ожидающих запросов.\n");

        Link distbLink = new Link(container, SWT.HORIZONTAL);
        distbLink.setText("Последнюю версию можно скачать по ссылке: \n"
                + "<a href=\"#\">" + DISTRIB_LINK + "</a>");
        distbLink.addListener(SWT.Selection, event -> Program.launch(DISTRIB_LINK));

        Link helpPageLink = new Link(container, SWT.HORIZONTAL);
        helpPageLink.setText("Страница продукта: \n"
                + "<a href=\"#\">" + HOMEPAGE + "</a>");
        helpPageLink.addListener(SWT.Selection, event -> Program.launch(HOMEPAGE));

        Link linkFAQ = new Link(container, SWT.HORIZONTAL);
        linkFAQ.setText("Свои вопросы можете задать на канале в Телеграмм: \n"
                + "<a href=\"#\">" + TELEGRAM_LINK + "</a>");
        linkFAQ.addListener(SWT.Selection, event -> Program.launch(TELEGRAM_LINK));

        Label copyrightLabel = new Label(container, SWT.HORIZONTAL);
        copyrightLabel.setText("© \"Technology\" LLC");

        Button ok = new Button(container, SWT.PUSH);
        ok.setText("OK");
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        PixelConverter pc = new PixelConverter(getParent());
        data.widthHint = pc.convertWidthInCharsToPixels(OK_BUTTON_HEIGHT_IN_CHARS);
        ok.setLayoutData(data);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                container.close();
            }
        });
        container.setDefaultButton(ok);
    }
}
