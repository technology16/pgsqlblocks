package ru.taximaxim.treeviewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import ru.taximaxim.treeviewer.dialog.ColumnConfigDialog;
import ru.taximaxim.treeviewer.filter.MyTreeViewerFilter;
import ru.taximaxim.treeviewer.listeners.DataUpdateListener;
import ru.taximaxim.treeviewer.models.IColumn;
import ru.taximaxim.treeviewer.models.MyTreeViewerDataSource;
import ru.taximaxim.treeviewer.tree.MyTreeViewerTable;
import ru.taximaxim.treeviewer.utils.ImageUtils;
import ru.taximaxim.treeviewer.utils.Images;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Основная форма
 */
public class MyTreeViewer extends Composite {

    // в составе таблица с данными
    //верхняя панель с кнопками
    //панель запускает обновление таблицы
    //панель запускает диалог
    //панель запускет фильтр
    private ToolBar toolBar;
    private ResourceBundle resourceBundle;
    private GridLayout mainLayout;
    private ToolItem updateToolItem;
    private ToolItem configColumnToolItem;
    private ToolItem filterToolItem;
    private MyTreeViewerTable tree;
    private MyTreeViewerDataSource dataSource;
    private MyTreeViewerFilter filter;
    private DataUpdateListener dataUpdateListener;


    public MyTreeViewer(Composite parent, int style) {
        super(parent, style);
        //this.resourceBundle = bundle;
        mainLayout = new GridLayout();
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayout(mainLayout);
        setLayoutData(data);
        createContent();
    }

    public void setDataSource(MyTreeViewerDataSource dataSource){
        this.dataSource = dataSource;
        tree.setDataSource(dataSource);
    }

    public MyTreeViewerTable getTree() {
        return tree;
    }

    public DataUpdateListener getDataUpdateListener() {
        return dataUpdateListener;
    }

    public void setDataUpdateListener(DataUpdateListener dataUpdateListener) {
        this.dataUpdateListener = dataUpdateListener;
    }

    private void createContent() {
        createToolItems();
        filter = new MyTreeViewerFilter(this, SWT.TOP);
        filter.hide();
        tree = new MyTreeViewerTable(MyTreeViewer.this, SWT.FILL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
    }

    private void createToolItems() {
        toolBar = new ToolBar(this, SWT.HORIZONTAL);
        updateToolItem = new ToolItem(toolBar, SWT.PUSH);
        updateToolItem.setImage(ImageUtils.getImage(Images.UPDATE));
        //updateToolItem.setToolTipText(Images.UPDATE.getDescription(resourceBundle));
        updateToolItem.addListener(SWT.Selection, event -> updateTreeViewerData());

        filterToolItem = new ToolItem(toolBar, SWT.PUSH);
        filterToolItem.setImage(ImageUtils.getImage(Images.FILTER));
        filterToolItem.addListener(SWT.Selection, event -> openFilter());
        //filterToolItem.setToolTipText(Images.TABLE.getDescription(resourceBundle));

        configColumnToolItem = new ToolItem(toolBar, SWT.PUSH);
        configColumnToolItem.setImage(ImageUtils.getImage(Images.TABLE));
        //configColumnToolItem.setToolTipText(Images.TABLE.getDescription(resourceBundle));
        configColumnToolItem.addListener(SWT.Selection, event -> openConfigColumnDialog());
    }

    private void openFilter() {
        if (filter.isVisible()) {
            filter.hide();
        }else filter.show();
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

    public void setFilters(List<IColumn> filters) {
        filter.setFilterList(filters);
    }
}
