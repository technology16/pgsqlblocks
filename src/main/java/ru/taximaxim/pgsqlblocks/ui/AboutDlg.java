package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;

public class AboutDlg extends Dialog {

    public static final String DISTRIB_LINK = "http://pgcodekeeper.ru/pgsqlblocks/";
    public static final String HOMEPAGE = "http://pgcodekeeper.ru/pgsqlblocks.html";
    public static final String TELEGRAM_LINK = "https://telegram.me/joinchat/Bxn1Zwh02WM96O-55GAryA";

    public AboutDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        parentShell.setText("О приложении");
    }

    public String open() {
        Shell shell = new Shell(getParent(), getStyle());
        shell.setText(getText());
        createContent(shell);
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return null;
    }

    private void createContent(Shell container){

        GridLayout layout = new GridLayout(1, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        container.setLayout(layout);

        GridData textGd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        textGd.widthHint = 250;

        Image logo = new Image(null,
                getClass().getClassLoader().getResourceAsStream("images/block-48x48.png"));
        Label logoLabel = new Label(container, SWT.HORIZONTAL);
        logoLabel.setImage(logo);

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
        ok.setLayoutData(data);
        ok.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                container.close();
            }
        });
        container.setDefaultButton(ok);
    }
}
