/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.common.ui;

import java.util.ResourceBundle;

import org.eclipse.swt.graphics.Image;

import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;
import ru.taximaxim.pgsqlblocks.utils.ImageUtils;

public class DBModelsViewLabelProvider extends TreeLabelProvider {

    private final ResourceBundle bundle;

    public DBModelsViewLabelProvider(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 0 && (element instanceof DBController)) {
            DBController controller = (DBController) element;
            return getImage(controller);
        } else {
            return null;
        }
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof String) {
            switch (columnIndex) {
            case 0:
                return element.toString();
            case 1:
                return "";
            default:
                return bundle.getString("undefined");
            }
        }

        DBController controller = (DBController) element;
        switch (columnIndex) {
        case 0:
            return controller.getModelName();
        case 1:
            return String.valueOf(controller.getProcessesCount());
        default:
            return bundle.getString("undefined");
        }
    }

    private Image getImage(DBController controller) {
        if (controller.isBlocked()) {
            return ImageUtils.getDecoratedBlockedImage(controller.getStatus().getStatusImage());
        }

        return ImageUtils.getImage(controller.getStatus().getStatusImage());
    }
}
