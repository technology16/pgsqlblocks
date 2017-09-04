package ru.taximaxim.pgsqlblocks.modules.blocksjournal.view;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.taximaxim.pgsqlblocks.common.FilterCondition;
import ru.taximaxim.pgsqlblocks.common.models.*;
import ru.taximaxim.pgsqlblocks.common.ui.*;
import ru.taximaxim.pgsqlblocks.dialogs.TMTreeViewerColumnsDialog;
import ru.taximaxim.pgsqlblocks.utils.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class BlocksJournalView extends ApplicationWindow implements DBBlocksJournalListener, DBProcessesFiltersViewListener {

    private static final Logger LOG = Logger.getLogger(BlocksJournalView.class);

    private ToolItem showFiltersViewToolItem;

    private TableViewer filesTable;

    private DBProcessesView processesView;

    private DBProcessInfoView processInfoView;

    private DBProcessesFiltersView filtersView;

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

        ToolBar toolBar = new ToolBar(contentComposite, SWT.TOP | SWT.HORIZONTAL);
        ToolItem updateFilesListToolItem = new ToolItem(toolBar, SWT.PUSH);
        updateFilesListToolItem.setImage(ImageUtils.getImage(Images.UPDATE));
        updateFilesListToolItem.setToolTipText(resourceBundle.getString("refresh_files_list"));
        updateFilesListToolItem.addListener(SWT.Selection, e -> getJournalFilesFromJournalsDir());

        showFiltersViewToolItem = new ToolItem(toolBar, SWT.CHECK);
        showFiltersViewToolItem.setImage(ImageUtils.getImage(Images.FILTER));
        showFiltersViewToolItem.setToolTipText(Images.FILTER.getDescription(resourceBundle));
        showFiltersViewToolItem.addListener(SWT.Selection, event ->
                setFiltersViewVisibility(showFiltersViewToolItem.getSelection()));
        showFiltersViewToolItem.setEnabled(false);

        ToolItem showColumnsDialogToolItem = new ToolItem(toolBar, SWT.PUSH);
        showColumnsDialogToolItem.setImage(ImageUtils.getImage(Images.TABLE));
        showColumnsDialogToolItem.setToolTipText(resourceBundle.getString("columns"));
        showColumnsDialogToolItem.addListener(SWT.Selection, event -> showProcessesViewColumnsDialog());

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

        filtersView = new DBProcessesFiltersView(resourceBundle, processesContentContainer, SWT.NONE);
        filtersView.addListener(this);
        filtersView.hide();

        processesView = new DBProcessesView(processesContentContainer, SWT.NONE);
        processesView.getTreeViewer().setDataSource(new DBBlocksJournalViewDataSource(resourceBundle));
        processesView.getTreeViewer().addSelectionChangedListener(this::processesViewSelectionChanged);
        processesView.getTreeViewer().setInput(blocksJournal.getFilteredProcesses());

        processInfoView = new DBProcessInfoView(resourceBundle, processesView, SWT.NONE);
        processInfoView.hideToolBar();
        processInfoView.hide();

        sashForm.setWeights(new int[] {20, 80});
        getJournalFilesFromJournalsDir();

        return super.createContents(parent);
    }

    private void setFiltersViewVisibility(boolean isVisible) {
        blocksJournal.getProcessesFilters().setEnabled(isVisible);
        if (isVisible) {
            filtersView.show();
        } else {
            filtersView.hide();
        }
    }

    private void filesTableSelectionChanged(SelectionChangedEvent event) {
        if (event.getSelection().isEmpty()) {
            showFiltersViewToolItem.setSelection(false);
            showFiltersViewToolItem.setEnabled(false);
            filesTable.setInput(null);
        } else {
            showFiltersViewToolItem.setSelection(false);
            showFiltersViewToolItem.setEnabled(true);
            filtersView.hide();
            filtersView.resetFiltersContent();

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
            this.journalFiles.addAll(Arrays.asList(files));
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

    private void showProcessesViewColumnsDialog() {
        TMTreeViewerColumnsDialog dialog = new TMTreeViewerColumnsDialog(resourceBundle, processesView.getTreeViewer(), getShell());
        dialog.open();
    }

    @Override
    public void dbBlocksJournalDidAddProcesses() {
        processesView.getTreeViewer().refresh();
    }

    @Override
    public void dbBlocksJournalDidCloseAllProcesses() {}

    @Override
    public void dbBlocksJournalDidCloseProcesses(List<DBBlocksJournalProcess> processes) {}

    @Override
    public void dbBlocksJournalDidChangeFilters() {
        processesView.getTreeViewer().refresh();
    }

    @Override
    public void processesFiltersViewPidFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition) {
        blocksJournal.getProcessesFilters().getPidFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewPidFilterValueChanged(DBProcessesFiltersView view, Integer value) {
        blocksJournal.getProcessesFilters().getPidFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewQueryFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition) {
        blocksJournal.getProcessesFilters().getQueryFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewQueryFilterValueChanged(DBProcessesFiltersView view, String value) {
        blocksJournal.getProcessesFilters().getQueryFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewApplicationFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition) {
        blocksJournal.getProcessesFilters().getApplicationFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewApplicationFilterValueChanged(DBProcessesFiltersView view, String value) {
        blocksJournal.getProcessesFilters().getApplicationFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewDatabaseFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition) {
        blocksJournal.getProcessesFilters().getDatabaseFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewDatabaseFilterValueChanged(DBProcessesFiltersView view, String value) {
        blocksJournal.getProcessesFilters().getDatabaseFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewUserNameFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition) {
        blocksJournal.getProcessesFilters().getUserNameFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewUserNameFilterValueChanged(DBProcessesFiltersView view, String value) {
        blocksJournal.getProcessesFilters().getUserNameFilter().setValue(value);
    }

    @Override
    public void processesFiltersViewClientFilterConditionChanged(DBProcessesFiltersView view, FilterCondition condition) {
        blocksJournal.getProcessesFilters().getClientFilter().setCondition(condition);
    }

    @Override
    public void processesFiltersViewClientFilterValueChanged(DBProcessesFiltersView view, String value) {
        blocksJournal.getProcessesFilters().getClientFilter().setValue(value);
    }
}
