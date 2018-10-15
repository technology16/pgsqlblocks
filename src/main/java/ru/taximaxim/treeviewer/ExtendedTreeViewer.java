package ru.taximaxim.treeviewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeColumn;
import ru.taximaxim.treeviewer.dialog.ColumnConfigDialog;
import ru.taximaxim.treeviewer.filter.FilterChangeHandler;
import ru.taximaxim.treeviewer.filter.FilterComposite;
import ru.taximaxim.treeviewer.l10n.TreeViewer;
import ru.taximaxim.treeviewer.listeners.TreeViewerSortColumnSelectionListener;
import ru.taximaxim.treeviewer.models.DataSource;
import ru.taximaxim.treeviewer.models.IObject;
import ru.taximaxim.treeviewer.models.ObjectViewComparator;
import ru.taximaxim.treeviewer.tree.ExtendedTreeViewerComponent;
import ru.taximaxim.treeviewer.utils.ImageUtils;
import ru.taximaxim.treeviewer.utils.Images;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Tree viewer that implements common logic. Comes with toolbar where filters and column selection are located.
 * <br>
 * Supports filtering, sorting, l10n, hiding columns.
 */
public class ExtendedTreeViewer<T extends IObject> extends Composite implements TreeViewerSortColumnSelectionListener {

    private ResourceBundle resourceBundle;
    private ExtendedTreeViewerComponent<T> tree;
    private FilterComposite filterComposite;
    private ObjectViewComparator comparator;
    private FilterChangeHandler filterChangeHandler;
    private DataSource<T> dataSource;
    private ToolItem filterToolItem;
    private Runnable updateToolItemAction;

    public ExtendedTreeViewer(Composite parent, int style, Object userData, DataSource<T> dataSource,
                              Locale locale) {
        super(parent, style);
        this.dataSource = dataSource;
        initResourceBundle(locale);
        GridLayout mainLayout = new GridLayout();
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(mainLayout);
        setLayoutData(data);
        filterChangeHandler = new FilterChangeHandler(dataSource);
        createContent();
        tree.setDataSource(dataSource);
        getTreeViewer().setInput(userData);
    }

    private void initResourceBundle(Locale locale) {
        resourceBundle = ResourceBundle.getBundle(TreeViewer.class.getName(),
                locale == null ? new Locale("ru") : locale);
    }

    public ExtendedTreeViewerComponent getTreeViewer() {
        return tree;
    }

    public void setComparator(ObjectViewComparator comparator) {
        this.comparator = comparator;
    }

    private void createContent() {
        createToolItems();
        filterComposite = new FilterComposite(this, SWT.TOP, resourceBundle, dataSource, filterChangeHandler);
        tree = new ExtendedTreeViewerComponent<>(this,
                 SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        filterChangeHandler.setTree(tree);
        filterComposite.hide();
        tree.addSortListener(this);
    }

    public ObjectViewComparator getComparator() {
        return comparator;
    }

    //If not called then null
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
                tree.refresh();
            } else {
                tree.refresh();
            }
        });

        filterToolItem = new ToolItem(toolBar, SWT.CHECK);
        filterToolItem.setSelection(false);
        filterToolItem.setImage(ImageUtils.getImage(Images.FILTER));
        filterToolItem.addListener(SWT.Selection, event -> openFilter());
        filterToolItem.setToolTipText(Images.FILTER.getDescription(resourceBundle));

        ToolItem configColumnToolItem = new ToolItem(toolBar, SWT.PUSH);
        configColumnToolItem.setImage(ImageUtils.getImage(Images.TABLE));
        configColumnToolItem.setToolTipText(Images.TABLE.getDescription(resourceBundle));
        configColumnToolItem.addListener(SWT.Selection, event -> openConfigColumnDialog());
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
}
