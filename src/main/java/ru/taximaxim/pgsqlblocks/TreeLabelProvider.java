package ru.taximaxim.pgsqlblocks;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

  public class TreeLabelProvider implements ITableLabelProvider {
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

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            return "";
        }
    }
