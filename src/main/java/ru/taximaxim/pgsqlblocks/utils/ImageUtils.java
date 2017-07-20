package ru.taximaxim.pgsqlblocks.utils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ImageUtils {

    private final static ImageUtils instance = new ImageUtils();

    private ImageUtils() {}

    private static final ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<>();

    public static Image getImage(Images type) {
        return imagesMap.computeIfAbsent(type.toString(),
                k -> new Image(null, instance.getClass().getClassLoader().getResourceAsStream(type.getImageAddr())));
    }

    public static Image decorateImage(Image image, ImageDescriptor imageDescriptor, int iconQuadrant) {
        DecorationOverlayIcon overlayIcon = new DecorationOverlayIcon(image, imageDescriptor, iconQuadrant);
        return overlayIcon.createImage();
    }
}
