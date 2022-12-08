/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017-2022 "Technology" LLC
 * %
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
package ru.taximaxim.pgsqlblocks.utils;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

public final class ImageUtils {

    private static final Map<Images, Image> IMAGES_MAP = new EnumMap<>(Images.class);
    private static final Map<Images, Image> IMAGES_DECORATED_MAP = new EnumMap<>(Images.class);

    private static final int BLOCKED_ICON_QUADRANT = IDecoration.TOP_RIGHT;

    private static final ImageDescriptor BLOCKED_DESCRIPTOR = ImageDescriptor
            .createFromURL(ImageUtils.class.getClassLoader()
                    .getResource(Images.DECORATOR_BLOCKED.getImageAddr()));

    public static Image getImage(Images type) {
        return IMAGES_MAP.computeIfAbsent(type,
                k -> new Image(null, ImageUtils.class.getClassLoader().getResourceAsStream(type.getImageAddr())));
    }

    public static Image getDecoratedBlockedImage(Images images) {
        return IMAGES_DECORATED_MAP.computeIfAbsent(images,
                e -> decorateImage(getImage(e), BLOCKED_DESCRIPTOR, BLOCKED_ICON_QUADRANT));
    }

    private static Image decorateImage(Image image, ImageDescriptor imageDescriptor, int iconQuadrant) {
        return new DecorationOverlayIcon(image, imageDescriptor, iconQuadrant).createImage();
    }

    private ImageUtils() {}
}
