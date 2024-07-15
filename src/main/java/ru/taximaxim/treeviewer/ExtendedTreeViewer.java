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
package ru.taximaxim.treeviewer;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.xmlstore.ColumnLayoutsXmlStore;
import ru.taximaxim.treeviewer.dialog.ColumnConfigDialog;
import ru.taximaxim.treeviewer.dialog.SaveDialog;
import ru.taximaxim.treeviewer.filter.FilterChangeHandler;
import ru.taximaxim.treeviewer.filter.FilterComposite;
import ru.taximaxim.treeviewer.l10n.TreeViewer;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.tree.ExtendedTreeViewerComponent;
import ru.taximaxim.treeviewer.utils.ImageUtils;
import ru.taximaxim.treeviewer.utils.Images;

/**
 * Tree viewer that implements common logic. Comes with toolbar where filters and column selection are located.
 * <br>
 * Supports filtering, sorting, l10n, hiding columns.
 */
public class ExtendedTreeViewer<T extends IObject> extends Composite {

    private ResourceBundle resourceBundle;
    private ExtendedTreeViewerComponent<T> tree;
    private FilterComposite filterComposite;
    private final FilterChangeHandler filterChangeHandler;
    private ToolItem filterToolItem;
    private Runnable updateToolItemAction;
    private final boolean isBlockJournalTab;
    private DBController controller;

    public ExtendedTreeViewer(Composite parent, int style, Object userData,
            DataSource<T> dataSource, Locale locale,
            ColumnLayoutsXmlStore columnLayoutsStore, boolean isBlockJournalTab) {
        super(parent, style);
        this.isBlockJournalTab = isBlockJournalTab;
        initResourceBundle(locale);
        GridLayout mainLayout = new GridLayout();
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(mainLayout);
        setLayoutData(data);
        filterChangeHandler = new FilterChangeHandler(dataSource);
        createContent(dataSource);
        tree.setData(dataSource, columnLayoutsStore);
        getTreeViewer().setInput(userData);
    }

    public ExtendedTreeViewer(Composite parent, int style, Object userData,
            DataSource<T> dataSource, Locale locale,
            ColumnLayoutsXmlStore columnLayoutsStore) {
        this(parent, style, userData, dataSource, locale, columnLayoutsStore, false);
    }

    public void setController(DBController controller) {
        this.controller = controller;
    }

    private void initResourceBundle(Locale locale) {
        resourceBundle = ResourceBundle.getBundle(TreeViewer.class.getName(),
                locale == null ? new Locale("ru") : locale);
    }

    public ExtendedTreeViewerComponent<T> getTreeViewer() {
        return tree;
    }

    private void createContent(DataSource<T> dataSource) {
        createToolItems();
        filterComposite = new FilterComposite(this, SWT.TOP | SWT.BORDER,
                resourceBundle, dataSource, filterChangeHandler);
        tree = new ExtendedTreeViewerComponent<>(this,
                SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        filterChangeHandler.setTree(tree);
        filterComposite.hide();
    }

    // If not called then null
    public void setUpdateButtonAction(Runnable runnable) {
        this.updateToolItemAction = runnable;
    }

    private void createToolItems() {
        ToolBar toolBar = new ToolBar(this, SWT.HORIZONTAL);

        ToolItem updateToolItem = new ToolItem(toolBar, SWT.PUSH);
        updateToolItem.setImage(ImageUtils.getImage(Images.UPDATE));
        updateToolItem.setToolTipText(Images.UPDATE.getDescription(resourceBundle));
        updateToolItem.addListener(SWT.Selection, event -> {
            if (updateToolItemAction != null) {
                updateToolItemAction.run();
            }
            tree.refreshWithoutSelection();
        });
        if (isBlockJournalTab) {
            ToolItem clearButton = new ToolItem(toolBar, SWT.PUSH);
            clearButton.setImage(ImageUtils.getImage(Images.CLEAN));
            clearButton.setToolTipText(Images.CLEAN.getDescription(resourceBundle));
            clearButton.addListener(SWT.Selection, event -> clean());
        }

        filterToolItem = new ToolItem(toolBar, SWT.CHECK);
        filterToolItem.setSelection(false);
        filterToolItem.setImage(ImageUtils.getImage(Images.FILTER));
        filterToolItem.addListener(SWT.Selection, event -> openFilter());
        filterToolItem.setToolTipText(Images.FILTER.getDescription(resourceBundle));

        ToolItem configColumnToolItem = new ToolItem(toolBar, SWT.PUSH);
        configColumnToolItem.setImage(ImageUtils.getImage(Images.TABLE));
        configColumnToolItem.setToolTipText(Images.TABLE.getDescription(resourceBundle));
        configColumnToolItem.addListener(SWT.Selection, event -> openConfigColumnDialog());

        if (isBlockJournalTab) {
            ToolItem saveJournals = new ToolItem(toolBar, SWT.PUSH);
            saveJournals.setImage(ImageUtils.getImage(Images.SAVE_ALL));
            saveJournals.setToolTipText(Images.SAVE_ALL.getDescription(resourceBundle));
            saveJournals.addListener(SWT.Selection, event -> openSaveDialog());
        }
    }

    private void openSaveDialog() {
        new SaveDialog(resourceBundle, controller, getShell()).open();
    }

    private void openFilter() {
        if (filterComposite.isVisible()) {
            filterComposite.hide();
            filterToolItem.setSelection(false);
        } else {
            filterComposite.show();
            filterToolItem.setSelection(true);
        }
    }

    private void openConfigColumnDialog() {
        new ColumnConfigDialog(resourceBundle, tree, this.getShell()).open();
    }

    public void clean() {
        if (controller != null) {
            controller.getBlocksJournal().getProcesses().clear();
        }
        tree.getTree().removeAll();
    }
}
