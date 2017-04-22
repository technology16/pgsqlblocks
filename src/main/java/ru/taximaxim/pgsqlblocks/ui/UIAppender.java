/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.ui;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UIAppender extends WriterAppender{

    private static final int TEXT_LIMIT = 20000;
    private Composite parent;
    private StyledText text;
    private StyledTextContent styledTextContent;
    private Display display;
    private boolean autoScrollEnabled = true;

    private ModifyListener modifyListener = (ModifyEvent e) -> {
        if (text.getText().length() > TEXT_LIMIT) {
            styledTextContent.replaceTextRange(0, text.getText().length() - TEXT_LIMIT - 1, "");
            styledTextContent.replaceTextRange(0, styledTextContent.getLine(0).length(), "");
        }
        if (autoScrollEnabled) {
            text.setTopIndex(text.getLineCount() - 1);
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

        styledTextContent = text.getContent();
    }

    @Override
    public void append(LoggingEvent event) {
        boolean displayIsFine = display == null || display.isDisposed();
        boolean parentIsFine = parent == null || parent.isDisposed();
        if(displayIsFine || parentIsFine || text == null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date time = new Date(event.getTimeStamp());
        String dateTime = sdf.format(time);
        String excMessage;
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