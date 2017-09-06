package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import ru.taximaxim.pgsqlblocks.TreeLabelProvider;
import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;
import ru.taximaxim.pgsqlblocks.utils.Images;

import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DBModelsViewLabelProvider extends TreeLabelProvider {

    private static final int BLOCKED_ICON_QUADRANT = IDecoration.TOP_RIGHT;
    private ConcurrentMap<String, ImageDescriptor> decoratorsMap = new ConcurrentHashMap<>();
    private final ResourceBundle bundle;

    public DBModelsViewLabelProvider(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 0) {
            DBController controller = (DBController) element;
            return getImage(controller);
        } else {
            return null;
        }
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        DBController controller = (DBController) element;
        switch (columnIndex) {
            case 0:
                return controller.getModel().getName();
            case 1:
                return String.valueOf(controller.getProcessesCount());
            default:
                return bundle.getString("undefined");
        }
    }

    private Image getImage(DBController controller) {
        Image image = ImageUtils.getImage(controller.getStatus().getStatusImage());

        if (controller.isBlocked()) {
            String decoratorBlockedPath = Images.DECORATOR_BLOCKED.getImageAddr();
            ImageDescriptor decoratorBlockedImageDesc = decoratorsMap.computeIfAbsent(decoratorBlockedPath, path -> {
                Image blockedImage = new Image(null, getClass().getClassLoader().getResourceAsStream(path));
                return ImageDescriptor.createFromImage(blockedImage);
            });
            image = ImageUtils.decorateImage(image, decoratorBlockedImageDesc, BLOCKED_ICON_QUADRANT);
        }
        return image;
    }
}
