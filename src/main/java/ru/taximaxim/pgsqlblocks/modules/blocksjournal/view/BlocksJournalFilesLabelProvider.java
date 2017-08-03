package ru.taximaxim.pgsqlblocks.modules.blocksjournal.view;

import org.eclipse.swt.graphics.Image;
import ru.taximaxim.pgsqlblocks.TreeLabelProvider;

import java.io.File;

public class BlocksJournalFilesLabelProvider  extends TreeLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        File file = (File)element;
        return file.getName();
    }

}
