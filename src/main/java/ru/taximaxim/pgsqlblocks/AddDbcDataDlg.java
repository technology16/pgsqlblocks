package ru.taximaxim.pgsqlblocks;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcDataListBuilder;

public class AddDbcDataDlg extends Dialog {

    private static final String DEFAULT_PORT = "5432";
    private static final int TEXT_WIDTH = 200;

    private Shell shell;
    private DbcData selectedDbcData;
    private Action action;

    private Text nameText;
    private Text hostText;
    private Text portText;
    private Text userText;
    private Text passwdText;
    private Text dbnameText;
    private Button enabledButton;
    
    private enum Action {
        ADD,
        EDIT;
    }
    
    public AddDbcDataDlg(Shell shell) {
        super(shell);
        this.shell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.action = Action.ADD;
    }
    
    public AddDbcDataDlg(Shell shell, DbcData dbcData) {
        super(shell);
        this.shell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.selectedDbcData = dbcData;
        this.action = Action.EDIT;
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
      nameLabel.setText("Имя соединения");
      nameText = new Text(container, SWT.BORDER);
      nameText.setLayoutData(textGd);
      
      Label hostLabel = new Label(container, SWT.HORIZONTAL);
      hostLabel.setText("Хост");
      hostText = new Text(container, SWT.BORDER);
      hostText.setLayoutData(textGd);
      
      Label portLabel = new Label(container, SWT.HORIZONTAL);
      portLabel.setText("Порт");
      portText = new Text(container, SWT.BORDER);
      portText.setText(DEFAULT_PORT);
      portText.setLayoutData(textGd);
      
      Label userLabel = new Label(container, SWT.HORIZONTAL);
      userLabel.setText("Имя пользователя");
      userText = new Text(container, SWT.BORDER);
      userText.setLayoutData(textGd);
      
      Label passwdLabel = new Label(container, SWT.HORIZONTAL);
      passwdLabel.setText("Пароль");
      passwdText = new Text(container, SWT.BORDER);
      passwdText.setLayoutData(textGd);
      passwdText.setEchoChar('•');
      passwdText.addListener(SWT.FocusOut, event -> {
          MessageDialog.openWarning(shell, "Внимание!", "Указание пароля здесь небезопасно. Используйте .pgpass файл.");
      });
      
      Label dbnameLabel = new Label(container, SWT.HORIZONTAL);
      dbnameLabel.setText("Имя БД");
      dbnameText = new Text(container, SWT.BORDER);
      dbnameText.setLayoutData(textGd);
      
      Label enabledLabel = new Label(container, SWT.HORIZONTAL);
      enabledLabel.setText("Подкл. автоматически");
      enabledButton = new Button(container, SWT.CHECK);
      
      if (action == Action.EDIT) {
          nameText.setText(selectedDbcData.getName());
          hostText.setText(selectedDbcData.getHost());
          portText.setText(selectedDbcData.getPort());
          dbnameText.setText(selectedDbcData.getDbname());
          userText.setText(selectedDbcData.getUser());
          passwdText.setText(selectedDbcData.getPasswd());
          enabledButton.setSelection(selectedDbcData.isEnabled());
      }

      return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      switch (action) {
      case ADD:
          newShell.setText("Добавить новое соединение");
          break;
      case EDIT:
          newShell.setText("Редактировать соединение");
          break;
      default:
          break;
      }
    }

    @Override
    protected Point getInitialSize() {
        return new Point(410, 330);
    }
    
    @Override
    protected void okPressed() {
        String name = nameText.getText();
        String host = hostText.getText();
        String port = portText.getText();
        String dbname = dbnameText.getText();
        String user = userText.getText();
        String passwd = passwdText.getText();
        boolean enabled = enabledButton.getSelection();
        DbcData newDbcData = new DbcData(name, host, port, dbname, user, passwd, enabled);
        switch (action) {
        case ADD:
            DbcDataListBuilder.getInstance().add(newDbcData);
            break;
        case EDIT:
            DbcDataListBuilder.getInstance().edit(selectedDbcData, newDbcData);
        }
        
        super.okPressed();
    }
}
