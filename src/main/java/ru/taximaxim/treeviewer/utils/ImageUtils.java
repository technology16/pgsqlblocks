package ru.taximaxim.treeviewer.utils;

import org.eclipse.swt.graphics.Image;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ImageUtils {

    private static final ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<>();

    public static Image getImage(Images type) {
        return imagesMap.computeIfAbsent(type.toString(),
                k -> new Image(null, ImageUtils.class.getClassLoader().getResourceAsStream(type.getImageAddr())));
    }
}
