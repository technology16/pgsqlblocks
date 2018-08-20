package ru.taximaxim.treeviewer.filter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.taximaxim.treeviewer.models.IColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * графическое представление фильтра
 */
public class MyTreeViewerFilter extends Composite {

    private GridLayout glayout;
    private List<IColumn> filterList = new ArrayList<>();


    public MyTreeViewerFilter( List<IColumn> filterlist, Composite parent, int style) {
        super(parent, style);
        filterList.addAll(filterlist);
        glayout = new GridLayout();
        glayout.marginWidth = 0;
        glayout.marginHeight = 0;
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        setLayout(glayout);
        setLayoutData(layoutData);
        createContent();
    }

    private void createContent() {
        glayout.numColumns = findColumnNumber();
        filterList.forEach(this::createFilterView);
    }

    private void createFilterView(IColumn filter) {
        Group group = new Group(this, SWT.HORIZONTAL);
        GridLayout layout = new GridLayout(3, false);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        group.setLayout(layout);
        group.setLayoutData(layoutData);

        GridData comboLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false,false);
        comboLayoutData.widthHint = 60;
        GridData textLayoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        textLayoutData.widthHint = 150;
        textLayoutData.minimumWidth = 150;

        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText(filter.getColumnName()); //bundle??????

        Combo combo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(comboLayoutData);
        List<FilterValues> filterValues = Arrays.asList(FilterValues.values());
        filterValues.forEach( f -> combo.add(f.toString()));
        // TODO: 20.08.18 listener!!!

        Text filterText = new Text(group, SWT.FILL | SWT.BORDER);
        filterText.setLayoutData(textLayoutData);
        // TODO: 20.08.18 listener!
    }


    private int findColumnNumber() {
        int i = filterList.size();
        if (i == 1) {
            return 1;
        }else if (i % 2 == 0) {
            return 2;
        } else {
            return 3;
        }
    }
}
