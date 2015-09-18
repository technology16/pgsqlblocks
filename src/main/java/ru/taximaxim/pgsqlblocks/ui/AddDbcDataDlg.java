package ru.taximaxim.pgsqlblocks.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.taximaxim.pgsqlblocks.DbcData;
import ru.taximaxim.pgsqlblocks.DbcDataList;

/**
 * Диалоговое окно добавления/редактирования подключения к БД
 * 
 * @author ismagilov_mg
 */
public class AddDbcDataDlg {
    
    private static final String DEFAULT_PORT = "5432";
    
    private Shell shell;
    private Shell parent;
    private Text nameText;
    private Text hostText;
    private Text portText;
    private Text userText;
    private Text passwdText;
    private Text dbnameText;
    private Button enabledButton;
    private Button onOk;
    private Button onCancel;
    private Action action;
    private DbcData editableDbc;
    
    private enum Action {
        ADD,
        EDIT
    }
    
    public AddDbcDataDlg(Shell parent) {
        this.parent = parent;
        createControls();
        addHandlers();
    }
    
    private void createControls() {
        GridData textGd = new GridData();
        textGd.widthHint = MainForm.TEXT_WIDTH;
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setLayout(new GridLayout(2, false));
        
        Label nameLabel = new Label(shell, SWT.HORIZONTAL);
        nameLabel.setText("Имя соединения");
        nameText = new Text(shell, SWT.BORDER);
        nameText.setLayoutData(textGd);
        
        Label hostLabel = new Label(shell, SWT.HORIZONTAL);
        hostLabel.setText("Хост");
        hostText = new Text(shell, SWT.BORDER);
        hostText.setLayoutData(textGd);
        
        Label portLabel = new Label(shell, SWT.HORIZONTAL);
        portLabel.setText("Порт");
        portText = new Text(shell, SWT.BORDER);
        portText.setLayoutData(textGd);
        
        Label userLabel = new Label(shell, SWT.HORIZONTAL);
        userLabel.setText("Имя пользователя");
        userText = new Text(shell, SWT.BORDER);
        userText.setLayoutData(textGd);
        
        Label passwdLabel = new Label(shell, SWT.HORIZONTAL);
        passwdLabel.setText("Пароль");
        passwdText = new Text(shell, SWT.BORDER);
        passwdText.setLayoutData(textGd);
        passwdText.setEchoChar('•');
        
        Label dbnameLabel = new Label(shell, SWT.HORIZONTAL);
        dbnameLabel.setText("Имя БД");
        dbnameText = new Text(shell, SWT.BORDER);
        dbnameText.setLayoutData(textGd);
        
        Label enabledLabel = new Label(shell, SWT.HORIZONTAL);
        enabledLabel.setText("Подкл. автоматически");
        enabledButton = new Button(shell, SWT.CHECK);
        
        onOk = new Button(shell, SWT.PUSH);
        GridData okGd = new GridData(SWT.BEGINNING, SWT.NONE,true, false);
        okGd.widthHint = MainForm.BTN_WIDTH;
        onOk.setLayoutData(okGd);
        onOk.setText("Ок");
        
        onCancel = new Button(shell, SWT.PUSH);
        GridData cancelGd = new GridData(SWT.END, SWT.NONE,true, false);
        cancelGd.widthHint = MainForm.BTN_WIDTH;
        onCancel.setLayoutData(cancelGd);
        onCancel.setText("Отмена");
        
        shell.pack();
    }

    private void addHandlers() {
        Listener closeListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.doit = false;
                hide();
            }
        };
        shell.addListener(SWT.Traverse, closeListener);
        shell.addListener(SWT.Close, closeListener);
        onCancel.addListener(SWT.Selection, closeListener);
        onOk.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event event) {
                String name = nameText.getText(); 
                String host = hostText.getText(); 
                String port = portText.getText(); 
                String dbname = dbnameText.getText();
                String user = userText.getText();
                String passwd = passwdText.getText();
                boolean enabled = enabledButton.getSelection();
                DbcData dbcData = new DbcData(name, host, port, dbname, user, passwd, enabled);
                switch(action){
                case ADD:
                    DbcDataList.getInstance().add(dbcData);
                    break;
                case EDIT:
                    DbcDataList.getInstance().edit(editableDbc, dbcData);
                    MainForm.getInstance().deleteServer(editableDbc);
                    break;
                }
                MainForm.getInstance().serverListUpdate();
                hide();
            }
        });
    }
    
    public void hide() {
        clearTextFields();
        shell.setVisible(false);
    }
    
    private void clearTextFields() {
        nameText.setText(""); 
        hostText.setText("");
        portText.setText(""); 
        dbnameText.setText(""); 
        userText.setText(""); 
        passwdText.setText(""); 
        enabledButton.setSelection(false);
    }
    
    public void show() {
        action = Action.ADD;
        shell.setText("Добавить новое соединение");
        portText.setText(DEFAULT_PORT);
        shell.open();
    }
    
    public void show(DbcData dbcData) {
        action = Action.EDIT;
        editableDbc = dbcData;
        shell.setText("Редактировать");
        nameText.setText(dbcData.getName());
        hostText.setText(dbcData.getHost());
        portText.setText(dbcData.getPort());
        dbnameText.setText(dbcData.getDbname());
        userText.setText(dbcData.getUser());
        passwdText.setText(dbcData.getPasswd());
        enabledButton.setSelection(dbcData.isEnabled());
        shell.open();
    }
}
