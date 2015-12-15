package ru.taximaxim.pgsqlblocks.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import ru.taximaxim.pgsqlblocks.utils.FilterProcess;

public class DateTimeSelectDlg extends Dialog {
    
    private DateTime calendar;
    private DateTime time;
    private FilterProcess filterProcess;
    private String field;

    public DateTimeSelectDlg(Shell shell, FilterProcess filterProcess, String field) {
        super(shell);
        this.filterProcess = filterProcess;
        this.field = field;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new RowLayout());

      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 1;
      parent.setLayout(gridLayout);
      calendar = new DateTime(container, SWT.BORDER | SWT.CALENDAR);
      time = new DateTime(container, SWT.BORDER | SWT.TIME);
      
      return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Выбор даты");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(350, 290);
    }
    
    @Override
    protected void okPressed() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ddd = LocalDateTime.of(calendar.getYear(), calendar.getMonth() + 1,
                calendar.getDay(), time.getHours(), time.getMinutes(), time.getSeconds());
        String fltDate = ddd.format(format);
        if (field.equals(filterProcess.getBackendStart().getKey())) {
            filterProcess.getBackendStart().setValue(fltDate);
        }
        if (field.equals(filterProcess.getQueryStart().getKey())) {
            filterProcess.getQueryStart().setValue(fltDate);
        }
        
        super.okPressed();
    }
    
}
