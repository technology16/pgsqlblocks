package ru.taximaxim.pgsqlblocks.dbcdata;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import ru.taximaxim.pgsqlblocks.utils.Images;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DbcDataListLabelProvider implements ITableLabelProvider {

    private static final int BLOCKED_ICON_QUADRANT = IDecoration.TOP_RIGHT;
    private static final int UPDATE_ICON_QUADRANT = IDecoration.BOTTOM_RIGHT;
    private List<ILabelProviderListener> listeners;

    private ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<String, Image>();

    /**
     * Constructs a FileTreeLabelProvider
     */
    public DbcDataListLabelProvider() {
        listeners = new ArrayList<ILabelProviderListener>();
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        DbcData dbcData = (DbcData) element;
        switch (columnIndex) {
        case 0:
            return getImage(dbcData);
        default:
            return null;
        }
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        DbcData dbcData = (DbcData) element;
        switch (columnIndex) {
        case 0:
            return dbcData.getName();
        default:
            return null;
        }
    }

    private Image getImage(DbcData dbcData) {
        String path = dbcData.getStatus().getImageAddr();
        Image image = imagesMap.get(path);
        if (image == null) {
            image = new Image(null, getClass().getClassLoader().getResourceAsStream(path));
            imagesMap.put(path, image);
        }

        if (dbcData.hasBlockedProcess()) {
            Image overlayImage = new Image(null,
                    getClass().getClassLoader().getResourceAsStream(Images.LITTLE_BLOCKED.getImageAddr()));
            ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(overlayImage);
            image = decorateImage(image, imageDescriptor, BLOCKED_ICON_QUADRANT);
        }
        if (dbcData.inUpdateState()){
            Image overlayImage = new Image(null,
                    getClass().getClassLoader().getResourceAsStream(Images.LITTLE_UPDATE.getImageAddr()));
            ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(overlayImage);
            image = decorateImage(image, imageDescriptor, UPDATE_ICON_QUADRANT);
        }
        return image;
    }

    private Image decorateImage(Image image, ImageDescriptor imageDescriptor, int iconQuadrant) {
        DecorationOverlayIcon overlayIcon = new DecorationOverlayIcon(
                image,
                imageDescriptor,
                iconQuadrant);
        return overlayIcon.createImage();
    }
}