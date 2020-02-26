/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
 * %
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
package ru.taximaxim.pgsqlblocks.modules.logs.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;

import ru.taximaxim.pgsqlblocks.ui.UIAppender;

public class LogsView extends Composite {

    private static final int TEXT_LIMIT = 20000;
    private StyledText text;
    private StyledTextContent styledTextContent;
    private boolean autoScrollEnabled = true;

    private final ModifyListener modifyListener = (ModifyEvent e) -> {
        if (text.getText().length() > TEXT_LIMIT) {
            styledTextContent.replaceTextRange(0, text.getText().length() - TEXT_LIMIT - 1, "");
            styledTextContent.replaceTextRange(0, styledTextContent.getLine(0).length(), "");
        }
        if (autoScrollEnabled) {
            text.setTopIndex(text.getLineCount() - 1);
        }
    };

    private final Listener logListener = e -> {
        Display display = getDisplay();
        if (display != null && !display.isDisposed()) {
            display.asyncExec(() -> {
                if (text != null && !text.isDisposed()) {
                    text.append(e.data.toString());
                }
            });
        }
    };

    public LogsView(Composite parent, int style) {
        super(parent, style);
        GridLayout layout = new GridLayout();
        layout.marginTop = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(layout);
        setLayoutData(layoutData);
        createContent();
        UIAppender.addListener(logListener);
    }

    private void createContent() {
        GridLayout layout = new GridLayout();
        layout.marginTop = 0;
        text = new StyledText(this, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        text.setLayout(layout);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setMargins(3, 3, 3, 3);
        text.layout(true);
        text.addModifyListener(modifyListener);

        layout(true, true);

        // add empty string on ENTER pressed
        text.addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_RETURN && !text.isDisposed()) {
                text.append("\n");
                text.setTopIndex(text.getLineCount() - 1);
                text.setCaretOffset(text.getCharCount() - 1);
            }
        });

        // wheel up and down
        text.addMouseWheelListener(e -> autoScrollEnabled = e.count <= 0);
        styledTextContent = text.getContent();
    }

    @Override
    public void dispose() {
        UIAppender.removeListener(logListener);
        super.dispose();
    }
}
