/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
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
package ru.taximaxim.pgsqlblocks.common.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

  public abstract class TreeLabelProvider implements ITableLabelProvider {
        // The listeners
        private List<ILabelProviderListener> listeners;

        protected ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<>();

        /**
         * Constructs a FileTreeLabelProvider
         */
        protected TreeLabelProvider() {
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
    }
