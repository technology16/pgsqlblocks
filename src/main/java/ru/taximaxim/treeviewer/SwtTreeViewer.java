package ru.taximaxim.treeviewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.treeviewer.dialog.ColumnConfigDialog;
import ru.taximaxim.treeviewer.filter.SwtTreeViewerFilter;
import ru.taximaxim.treeviewer.filter.SwtViewFilter;
import ru.taximaxim.treeviewer.listeners.DataUpdateListener;
import ru.taximaxim.treeviewer.listeners.MyTreeViewerSortColumnSelectionListener;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;
import ru.taximaxim.treeviewer.models.ObjectViewComparator;
import ru.taximaxim.treeviewer.tree.SwtTreeViewerTable;
import ru.taximaxim.treeviewer.utils.ImageUtils;
import ru.taximaxim.treeviewer.utils.Images;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main class with toolbar, filters, table and listeners
 */
public class SwtTreeViewer extends Composite implements MyTreeViewerSortColumnSelectionListener{

    private ResourceBundle outerResourceBundle;
    private ResourceBundle innerResourceBundle;
    private SwtTreeViewerTable tree;
    private SwtTreeViewerFilter viewerFilter;
    private DataUpdateListener dataUpdateListener;
    private ObjectViewComparator comparator;
    private SwtViewFilter myViewFilter;
    private MyTreeViewerDataSource dataSource;

    public SwtTreeViewer(Composite parent, int style, Object userData, MyTreeViewerDataSource dataSource,
                         ResourceBundle resourceBundle) {
        super(parent, style);
        this.outerResourceBundle = resourceBundle;
        this.dataSource = dataSource;
        initResourceBundle(outerResourceBundle);
        GridLayout mainLayout = new GridLayout();
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(mainLayout);
        setLayoutData(data);
        createContent();
        tree.setDataSource(dataSource);
        myViewFilter = new SwtViewFilter(dataSource, tree);
        getTree().setInput(userData);
    }

    private void initResourceBundle(ResourceBundle outerResourceBundle) {
        Locale locale =  new Locale("ru");
        if (outerResourceBundle != null) {
            locale = outerResourceBundle.getLocale();
        }
        innerResourceBundle = ResourceBundle.getBundle(ru.taximaxim.treeviewer.l10n.MyTreeViewer.class.getName(), locale);
    }

    public SwtTreeViewerTable getTree() {
        return tree;
    }

    public void setComparator(ObjectViewComparator comparator) {
        this.comparator = comparator;
    }

    private void createContent() {
        createToolItems();
        viewerFilter = new SwtTreeViewerFilter(this, SWT.TOP, innerResourceBundle, dataSource);
        viewerFilter.hide();
        tree = new SwtTreeViewerTable(SwtTreeViewer.this, SWT.FILL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        dataUpdateListener = new DataUpdateListener() {
            @Override
            public void needUpdateData() {
                tree.refresh();
            }
        };
        tree.addSortListener(this);

    }

    public ObjectViewComparator getComparator() {
        return comparator;
    }

    private void createToolItems() {
        ToolBar toolBar = new ToolBar(this, SWT.HORIZONTAL);
        ToolItem updateToolItem = new ToolItem(toolBar, SWT.PUSH);
        updateToolItem.setImage(ImageUtils.getImage(Images.UPDATE));
        updateToolItem.setToolTipText(Images.UPDATE.getDescription(innerResourceBundle));
        updateToolItem.addListener(SWT.Selection, event -> updateTreeViewerData());

        ToolItem filterToolItem = new ToolItem(toolBar, SWT.PUSH);
        filterToolItem.setImage(ImageUtils.getImage(Images.FILTER));
        filterToolItem.addListener(SWT.Selection, event -> openFilter());
        filterToolItem.setToolTipText(Images.FILTER.getDescription(innerResourceBundle));

        ToolItem configColumnToolItem = new ToolItem(toolBar, SWT.PUSH);
        configColumnToolItem.setImage(ImageUtils.getImage(Images.TABLE));
        configColumnToolItem.setToolTipText(Images.TABLE.getDescription(innerResourceBundle));
        configColumnToolItem.addListener(SWT.Selection, event -> openConfigColumnDialog());
    }

    private void openFilter() {
        if (viewerFilter.isVisible()) {
            viewerFilter.hide();
        }else viewerFilter.show();
    }

    private void updateTreeViewerData() {
        if (dataUpdateListener != null) {
            dataUpdateListener.needUpdateData();
        }

    }

    private void openConfigColumnDialog() {
        ColumnConfigDialog dialog = new ColumnConfigDialog(innerResourceBundle, tree, this.getShell());
        dialog.open();

    }

    @Override
    public void didSelectSortColumn(TreeColumn column, int sortDirection) {
        if (comparator != null) {
            comparator.setColumn(column);
            comparator.setSortDirection(sortDirection);
            tree.setComparator(null);
            tree.setComparator(comparator);
        }
    }

    /**
     * List of columns for filter view
     */
    public void setColumnsForFilterView(List<? extends IColumn> filters) {
        viewerFilter.setFilterList(filters, dataUpdateListener, myViewFilter);
    }
}
