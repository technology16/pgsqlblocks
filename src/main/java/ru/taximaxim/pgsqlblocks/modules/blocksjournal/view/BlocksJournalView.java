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

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.taximaxim.pgsqlblocks.common.models.*;
import ru.taximaxim.pgsqlblocks.common.ui.DBBlocksJournalViewDataSource;
import ru.taximaxim.pgsqlblocks.common.ui.DBProcessInfoView;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.Settings;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;
import ru.taximaxim.treeviewer.ExtendedTreeViewer;
import ru.taximaxim.pgsqlblocks.dialogs.DBProcessInfoDialog;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class BlocksJournalView extends ApplicationWindow implements DBBlocksJournalListener {

    private static final Logger LOG = Logger.getLogger(BlocksJournalView.class);

    private TableViewer filesTable;

    private ExtendedTreeViewer<DBProcess> processesView;

    private DBProcessInfoView processInfoView;

    private final ResourceBundle resourceBundle;

    private final List<File> journalFiles = new ArrayList<>();

    private XmlDocumentWorker xmlDocumentWorker = new XmlDocumentWorker();

    private final DBBlocksJournal blocksJournal = new DBBlocksJournal();

    private static DBBlocksJournalProcessSerializer serializer = new DBBlocksJournalProcessSerializer();

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
        processesView = new ExtendedTreeViewer<>(processesContentContainer, SWT.NONE, blocksJournal.getProcesses(),
               dbBlocksJournalViewDataSource , resourceBundle.getLocale());
        processesView.getTreeViewer().addSelectionChangedListener(this::processesViewSelectionChanged);
        processesView.getTreeViewer().getTree().addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                IStructuredSelection structuredSelection = processesView.getTreeViewer().getStructuredSelection();
                List<DBBlocksJournalProcess> selectedProcesses = (List<DBBlocksJournalProcess>) structuredSelection.toList();
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
        DBProcessInfoDialog dbProcessInfoView = new DBProcessInfoDialog(resourceBundle, this.getShell(), dbProcess, true);
        dbProcessInfoView.open();
    }

    private void filesTableSelectionChanged(SelectionChangedEvent event) {
        if (event.getSelection().isEmpty()) {
            filesTable.setInput(null);
        } else {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            File selectedFile = (File) selection.getFirstElement();
            openJournalFile(selectedFile);
        }
    }

    private void openJournalFile(File journalFile) {
        try {
            Document document = xmlDocumentWorker.openJournalFile(journalFile);
            List<DBBlocksJournalProcess> processes = new ArrayList<>();
            NodeList journalElements = document.getElementsByTagName(DBBlocksJournalProcessSerializer.JOURNAL_PROCESS_ROOT_ELEMENT_TAG_NAME);
            for (int i = 0; i < journalElements.getLength(); i++) {
                Node journalNode = journalElements.item(i);
                if (journalNode.getNodeType() == Node.ELEMENT_NODE) {
                    DBBlocksJournalProcess journalProcess = serializer.deserialize((Element)journalNode);
                    processes.add(journalProcess);
                }
            }
            blocksJournal.setJournalProcesses(processes);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOG.warn("Ошибка открытия файла журнала: " + e.getMessage());
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
