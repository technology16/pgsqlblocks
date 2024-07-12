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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.jface.viewers.ITreeContentProvider;

import ru.taximaxim.pgsqlblocks.modules.db.controller.DBController;

public class DBModelsViewContentProvider implements ITreeContentProvider {

    private final String DEFAULT_DB_GROUP = "default_db_group";

    private final ResourceBundle bundle;
    private final Map<String, List<DBController>> map = new LinkedHashMap<>();

    public DBModelsViewContentProvider(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        map.clear();
        if (inputElement instanceof List) {
            for (Object o : (List<?>) inputElement) {
                DBController el = (DBController) o;
                String dbGroup = getDbGroup(el.getModel().getDbGroup());
                map.computeIfAbsent(dbGroup, e -> new ArrayList<>()).add(el);
            }
            if (hasntGroup()) {
                return map.get(bundle.getString(DEFAULT_DB_GROUP)).toArray();
            }
        }
        return map.keySet().toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof String) {
            return map.get(parentElement).toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof DBController) {
            String dbGroup = getDbGroup(((DBController) element).getModel().getDbGroup());
            if (hasntGroup()) {
                return null;
            }
            return dbGroup;
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof String;
    }

    private boolean hasntGroup() {
        return map.size() == 1 && map.containsKey(bundle.getString(DEFAULT_DB_GROUP));
    }
    
    private String getDbGroup(String DbGroup) {
        return DbGroup.isEmpty() ? bundle.getString(DEFAULT_DB_GROUP) : DbGroup;
    }
}
