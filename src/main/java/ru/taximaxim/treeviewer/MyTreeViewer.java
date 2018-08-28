package ru.taximaxim.treeviewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.treeviewer.dialog.ColumnConfigDialog;
import ru.taximaxim.treeviewer.filter.MyTreeViewerFilter;
import ru.taximaxim.treeviewer.filter.MyViewFilter;
import ru.taximaxim.treeviewer.listeners.DataUpdateListener;
import ru.taximaxim.treeviewer.listeners.MyTreeViewerSortColumnSelectionListener;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;
import ru.taximaxim.treeviewer.models.ObjectViewComparator;
import ru.taximaxim.treeviewer.tree.MyTreeViewerTable;
import ru.taximaxim.treeviewer.utils.ImageUtils;
import ru.taximaxim.treeviewer.utils.Images;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Main class with toolbar, filters, table and listeners
 */
public class MyTreeViewer extends Composite implements MyTreeViewerSortColumnSelectionListener{

    private ResourceBundle resourceBundle;
    private MyTreeViewerTable tree;
    private MyTreeViewerFilter viewerFilter;
    private DataUpdateListener dataUpdateListener;
    private ObjectViewComparator comparator;
    private MyViewFilter myViewFilter;

    public MyTreeViewer(Composite parent, int style, Object userData, MyTreeViewerDataSource dataSource) {
        super(parent, style);
        //this.resourceBundle = bundle;
        GridLayout mainLayout = new GridLayout();
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(mainLayout);
        setLayoutData(data);
        createContent();
        tree.setDataSource(dataSource);
        myViewFilter = new MyViewFilter(dataSource, tree);
        getTree().setInput(userData);
    }

    public MyTreeViewerTable getTree() {
        return tree;
    }

    public void setComparator(ObjectViewComparator comparator) {
        this.comparator = comparator;
    }

    private void createContent() {
        createToolItems();
        viewerFilter = new MyTreeViewerFilter(this, SWT.TOP);
        viewerFilter.hide();
        tree = new MyTreeViewerTable(MyTreeViewer.this, SWT.FILL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
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
        //updateToolItem.setToolTipText(Images.UPDATE.getDescription(resourceBundle));
        updateToolItem.addListener(SWT.Selection, event -> updateTreeViewerData());

        ToolItem filterToolItem = new ToolItem(toolBar, SWT.PUSH);
        filterToolItem.setImage(ImageUtils.getImage(Images.FILTER));
        filterToolItem.addListener(SWT.Selection, event -> openFilter());
        //filterToolItem.setToolTipText(Images.TABLE.getDescription(resourceBundle));

        ToolItem configColumnToolItem = new ToolItem(toolBar, SWT.PUSH);
        configColumnToolItem.setImage(ImageUtils.getImage(Images.TABLE));
        //configColumnToolItem.setToolTipText(Images.TABLE.getDescription(resourceBundle));
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
        ColumnConfigDialog dialog = new ColumnConfigDialog(resourceBundle, tree, this.getShell());
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
