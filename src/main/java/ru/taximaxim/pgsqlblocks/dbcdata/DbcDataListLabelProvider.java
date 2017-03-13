/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
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

    private ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, ImageDescriptor> decoratorsMap = new ConcurrentHashMap<>();

    /**
     * Constructs a FileTreeLabelProvider
     */
    public DbcDataListLabelProvider() {
        listeners = new ArrayList<>();
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
        String imagePath = dbcData.getStatus().getStatusImage().getImageAddr();
        Image image = imagesMap.computeIfAbsent(imagePath, 
                k -> new Image(null, getClass().getClassLoader().getResourceAsStream(k)));

        if (dbcData.hasBlockedProcess()) {
            String decoratorBlockedPath = Images.DECORATOR_BLOCKED.getImageAddr();
            ImageDescriptor decoratorBlockedImageDesc = decoratorsMap.computeIfAbsent(decoratorBlockedPath, path -> {
                Image blockedImage = new Image(null, getClass().getClassLoader().getResourceAsStream(path));
                return ImageDescriptor.createFromImage(blockedImage);
            });

            image = decorateImage(image, decoratorBlockedImageDesc, BLOCKED_ICON_QUADRANT);
        }

        if (dbcData.isInUpdateState()){
            String decoratorUpdatePath = Images.DECORATOR_UPDATE.getImageAddr();
            ImageDescriptor decoratorUpdateImageDesc = decoratorsMap.computeIfAbsent(decoratorUpdatePath, path -> {
                Image updateImage = new Image(null, getClass().getClassLoader().getResourceAsStream(path));
                return ImageDescriptor.createFromImage(updateImage);
            });

            Image old = image;
            image = decorateImage(image, decoratorUpdateImageDesc, UPDATE_ICON_QUADRANT);
            if (dbcData.hasBlockedProcess()) {
                old.dispose();
            }
        }
        return image;
    }

    private Image decorateImage(Image image, ImageDescriptor imageDescriptor, int iconQuadrant) {
        DecorationOverlayIcon overlayIcon = new DecorationOverlayIcon(image, imageDescriptor, iconQuadrant);
        return overlayIcon.createImage();
    }
}