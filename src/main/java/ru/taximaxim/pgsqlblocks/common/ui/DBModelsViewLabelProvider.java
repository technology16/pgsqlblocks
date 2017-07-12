package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.swt.graphics.Image;
import ru.taximaxim.pgsqlblocks.TreeLabelProvider;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;

public class DBModelsViewLabelProvider extends TreeLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        DBController controller = (DBController) element;
        return getImage(controller);
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        DBController controller = (DBController) element;
        return controller.getModel().getName();
    }
    private Image getImage(DBController controller) {
        String imagePath = controller.getStatus().getStatusImage().getImageAddr();
        Image image = imagesMap.computeIfAbsent(imagePath,
                k -> new Image(null, getClass().getClassLoader().getResourceAsStream(k)));
        return image;
    }
}
