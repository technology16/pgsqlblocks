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
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcessSerializer;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.common.ui.DBBlocksJournalViewDataSource;
import ru.taximaxim.pgsqlblocks.common.ui.DBProcessInfoView;
import ru.taximaxim.pgsqlblocks.common.ui.DBProcessesView;
import ru.taximaxim.pgsqlblocks.dialogs.TMTreeViewerColumnsDialog;
import ru.taximaxim.pgsqlblocks.utils.*;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class BlocksJournalView extends ApplicationWindow {

    private static final Logger LOG = Logger.getLogger(BlocksJournalView.class);

    private TableViewer filesTable;

    private DBProcessesView processesView;

    private DBProcessInfoView processInfoView;

    private final Settings settings;
    private final ResourceBundle resourceBundle;

    private final List<File> journalFiles = new ArrayList<>();

    private XmlDocumentWorker xmlDocumentWorker = new XmlDocumentWorker();

    private static DBBlocksJournalProcessSerializer serializer = new DBBlocksJournalProcessSerializer();

    public BlocksJournalView(Settings settings) {
        super(null);
        this.settings = settings;
        this.resourceBundle = settings.getResourceBundle();
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
/*
        ToolItem openJournalDirectoryToolItem = new ToolItem(toolBar, SWT.PUSH);
        openJournalDirectoryToolItem.setImage(ImageUtils.getImage(Images.FOLDER));
        openJournalDirectoryToolItem.setToolTipText(resourceBundle.getString("open_dir"));
        openJournalDirectoryToolItem.addListener(SWT.Selection, event -> {
            try {
                Desktop.getDesktop().open(PathBuilder.getInstance().getBlocksJournalsDir().toFile());
            } catch (IOException exception) {
                LOG.error(exception.getMessage(), exception);
            }
        });
*/
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

        processesView = new DBProcessesView(sashForm, SWT.NONE);
        processesView.getTreeViewer().setDataSource(new DBBlocksJournalViewDataSource(resourceBundle));
        processesView.getTreeViewer().addSelectionChangedListener(this::processesViewSelectionChanged);

        processInfoView = new DBProcessInfoView(resourceBundle, processesView, SWT.NONE);
        processInfoView.hideToolBar();
        processInfoView.hide();

        sashForm.setWeights(new int[] {30, 70});
        getJournalFilesFromJournalsDir();

        return super.createContents(parent);
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
            processesView.getTreeViewer().setInput(processes);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    private void getJournalFilesFromJournalsDir() {
        this.journalFiles.clear();
        Path blocksJournalsDirPath = PathBuilder.getInstance().getBlocksJournalsDir();
        List<File> journalFiles = Arrays.asList(blocksJournalsDirPath.toFile().listFiles());
        this.journalFiles.addAll(journalFiles);
        filesTable.refresh();
    }

    private void processesViewSelectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection.isEmpty()) {
            processInfoView.hide();
        } else {
            DBProcess process = null;
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

}
