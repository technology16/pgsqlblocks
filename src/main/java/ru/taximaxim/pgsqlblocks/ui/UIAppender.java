package ru.taximaxim.pgsqlblocks.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;

public class UIAppender extends WriterAppender{

    private static final int TEXT_LIMIT = 5000;
    private Composite parent;
    private StyledText text;
    private Display display;
    private boolean autoScrollEnabled = true;
    private ModifyListener modifyListener = new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
            if(text.getText().length() > TEXT_LIMIT) {
                StyledTextContent styledTextContent = text.getContent();
                // cut excess text
                styledTextContent.replaceTextRange(0,text.getText().length() - TEXT_LIMIT - 1, "");
                // cut lopped line
                styledTextContent.replaceTextRange(0, styledTextContent.getLine(0).length(), "");
                text.setContent(styledTextContent);
            }
            if(autoScrollEnabled) {
                text.setTopIndex(text.getLineCount() - 1);
            }
        }
    };

    public UIAppender(Composite parent) {
        this.parent = parent;
        this.display = parent.getDisplay();
        createControl();
    }

    private void createControl() {
        text = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setMargins(3, 3, 3, 3);
        text.layout(true);
        text.addModifyListener(modifyListener);
        parent.layout(true, true);

        // add empty string on ENTER pressed
        text.addTraverseListener(e -> {
            switch (e.detail) {
                case SWT.TRAVERSE_RETURN:
                    if (!text.isDisposed()) {
                        text.append("\n");
                        text.setTopIndex(text.getLineCount() - 1);
                        text.setCaretOffset(text.getCharCount() - 1);
                    }
                    break;
                default:
                    break;
            }
        });

        // wheel up and down
        text.addMouseWheelListener(e -> autoScrollEnabled = e.count <= 0);
    }

    public void append(LoggingEvent event) {
        boolean displayIsFine = display == null || display.isDisposed();
        boolean parentIsFine = parent == null || parent.isDisposed();
        if(displayIsFine || parentIsFine || text == null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date time = new Date(event.getTimeStamp());
        String dateTime = sdf.format(time);
        String excMessage = "";
        Object message = event.getMessage();
        if (message instanceof String) {
            excMessage = (String) message;
        } else {
            return;
        }
        final String logMessage = String.format("[%s] %s%n", dateTime, excMessage);
        parent.getDisplay().asyncExec(() -> {
            if (!text.isDisposed()) {
                text.append(logMessage);
            }
        });
    }
}