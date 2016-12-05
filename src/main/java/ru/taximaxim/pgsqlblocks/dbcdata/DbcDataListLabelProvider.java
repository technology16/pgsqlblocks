package ru.taximaxim.pgsqlblocks.dbcdata;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import ru.taximaxim.pgsqlblocks.process.ProcessStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DbcDataListLabelProvider implements ITableLabelProvider {

    private static final int QUADRANT = IDecoration.TOP_RIGHT;
    private static final double RESIZE_SCALE = 0.5;

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
            String overlayImagePath = ProcessStatus.BLOCKING.getImageAddr();
            Image overlayImage = new Image(null,
                    getClass().getClassLoader().getResourceAsStream(
                            overlayImagePath));
            ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage
                    (resize(overlayImage));
            return decorateImage(image, imageDescriptor);
        } else {
            return image;
        }
    }

    private Image decorateImage(Image image, ImageDescriptor imageDescriptor) {
        DecorationOverlayIcon overlayIcon = new DecorationOverlayIcon(
                image,
                imageDescriptor,
                QUADRANT);
        return overlayIcon.createImage();
    }

    private Image resize(Image image) {
        int width = (int) (image.getImageData().width * RESIZE_SCALE);
        int height = (int) (image.getImageData().height * RESIZE_SCALE);
        Image scaled = new Image(Display.getDefault(), width, height);
        GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(image, 0, 0,
                image.getBounds().width, image.getBounds().height,
                0, 0,
                width, height);
        gc.dispose();
        image.dispose();
        return scaled;
    }
}