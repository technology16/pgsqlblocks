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
package ru.taximaxim.pgsqlblocks.modules.blocksjournal.view;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournal;
import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalListener;
import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.common.ui.DBBlocksJournalViewDataSource;
import ru.taximaxim.pgsqlblocks.common.ui.DBProcessInfoView;
import ru.taximaxim.pgsqlblocks.dialogs.DBProcessInfoDialog;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.Settings;
import ru.taximaxim.pgsqlblocks.xmlstore.ColumnLayoutsXmlStore;
import ru.taximaxim.pgsqlblocks.xmlstore.DBBlocksXmlStore;
import ru.taximaxim.treeviewer.ExtendedTreeViewer;

public class BlocksJournalView extends ApplicationWindow implements DBBlocksJournalListener {

    private static final String BLOCKS_JOURNAL_COLUMNS = "blocksJournalColumns.xml";

    private TableViewer filesTable;

    private ExtendedTreeViewer<DBProcess> processesView;

    private DBProcessInfoView processInfoView;

    private final ResourceBundle resourceBundle;

    private final List<File> journalFiles = new ArrayList<>();

    private final DBBlocksJournal blocksJournal = new DBBlocksJournal();

    public BlocksJournalView(Settings settings) {
        super(null);
        this.resourceBundle = settings.getResourceBundle();
        blocksJournal.addListener(this);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setMinimumSize(new Point(800,600));
        shell.setText(resourceBundle.getString("saved_blocks_journals"));
    }

    @Override
    protected Point getInitialSize() {
        return new Point(1024,768);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite contentComposite = new Composite(parent, SWT.NONE);
        contentComposite.setLayout(new GridLayout());

        SashForm sashForm = new SashForm(contentComposite, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        filesTable = new TableViewer(sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        filesTable.getTable().setHeaderVisible(true);
        TableViewerColumn column = new TableViewerColumn(filesTable, SWT.NONE);
        column.getColumn().setText(resourceBundle.getString("journals"));
        column.getColumn().setWidth(300);
        filesTable.setLabelProvider(new BlocksJournalFilesLabelProvider());
        filesTable.setContentProvider(new BlocksJournalFilesContentProvider());
        filesTable.setInput(journalFiles);
        filesTable.addSelectionChangedListener(this::filesTableSelectionChanged);

        Composite processesContentContainer = new Composite(sashForm, SWT.NONE);
        processesContentContainer.setLayout(new GridLayout());
        processesContentContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        DBBlocksJournalViewDataSource dbBlocksJournalViewDataSource = new DBBlocksJournalViewDataSource(resourceBundle);
        processesView = new ExtendedTreeViewer<>(processesContentContainer, SWT.NONE,
                blocksJournal.getProcesses(), dbBlocksJournalViewDataSource,
                resourceBundle.getLocale(), new ColumnLayoutsXmlStore(BLOCKS_JOURNAL_COLUMNS));
        processesView.getTreeViewer().addSelectionChangedListener(this::processesViewSelectionChanged);
        processesView.getTreeViewer().getTree().addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                IStructuredSelection structuredSelection = processesView.getTreeViewer().getStructuredSelection();
                List<?> selectedProcesses = structuredSelection.toList();
                openProcessDialogInfo(selectedProcesses.get(0));
            }
        });

        processInfoView = new DBProcessInfoView(resourceBundle, processesView, SWT.NONE);
        processInfoView.hideToolBar();
        processInfoView.hide();

        sashForm.setWeights(new int[] {20, 80});
        getJournalFilesFromJournalsDir();

        return super.createContents(parent);
    }

    private void openProcessDialogInfo(Object dbProcess){
        new DBProcessInfoDialog(resourceBundle, this.getShell(), dbProcess, true).open();
    }

    private void filesTableSelectionChanged(SelectionChangedEvent event) {
        if (event.getSelection().isEmpty()) {
            filesTable.setInput(null);
        } else {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            File selectedFile = (File) selection.getFirstElement();
            DBBlocksXmlStore store = new DBBlocksXmlStore(selectedFile.getName());
            List<DBBlocksJournalProcess> processes = store.readObjects();
            blocksJournal.setJournalProcesses(processes);
        }
    }

    private void getJournalFilesFromJournalsDir() {
        this.journalFiles.clear();
        Path blocksJournalsDirPath = PathBuilder.getInstance().getBlocksJournalsDir();
        File[] files = blocksJournalsDirPath.toFile().listFiles();
        if (files != null) {
            List<File> filesList = Arrays.asList(files);
            filesList.sort(Comparator.comparingLong(File::lastModified));
            this.journalFiles.addAll(filesList);
        }
        filesTable.refresh();
    }

    private void processesViewSelectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection.isEmpty()) {
            processInfoView.hide();
        } else {
            DBProcess process;
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            Object element = structuredSelection.getFirstElement();
            if (element instanceof DBBlocksJournalProcess) {
                DBBlocksJournalProcess blocksJournalProcess = (DBBlocksJournalProcess)element;
                process = blocksJournalProcess.getProcess();
            } else {
                process = (DBProcess)element;
            }
            processInfoView.show(process);
        }
    }

    @Override
    public void dbBlocksJournalDidAddProcesses() {
        processesView.getTreeViewer().refresh();
    }

    @Override
    public void dbBlocksJournalDidCloseAllProcesses() {}

    @Override
    public void dbBlocksJournalDidCloseProcesses(List<DBBlocksJournalProcess> processes) {}
}
