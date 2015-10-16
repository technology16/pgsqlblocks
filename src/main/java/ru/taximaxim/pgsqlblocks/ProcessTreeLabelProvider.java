package ru.taximaxim.pgsqlblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class ProcessTreeLabelProvider implements ITableLabelProvider {
    // The listeners
    private List<ILabelProviderListener> listeners;

    private ConcurrentMap<String, Image> imagesMap = new ConcurrentHashMap<String, Image>();

    /**
     * Constructs a FileTreeLabelProvider
     */
    public ProcessTreeLabelProvider() {
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
        Process process = (Process) element;
        switch (columnIndex) {
        case 0: return getImage(process.getStatus().getImageAddr());
        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        Process process = (Process) element;
        switch (columnIndex) {
            case 0: return String.valueOf(process.getPid());
            case 1: return String.valueOf(process.getChildren().size());
            case 2: return process.getApplicationName();
            case 3: return process.getDatname();
            case 4: return process.getUsename();
            case 5: return process.getClient();
            case 6: return process.getBackendStart();
            case 7: return process.getQueryStart();
            case 8: return process.getXactStart();
            case 9: return process.getState();
            case 10: return process.getStateChange();
            case 11: return String.valueOf(process.getBlockedBy());
            case 12: return String.valueOf(process.getBlockingLocks());
            case 13: return process.getQuery();
            case 14: return String.valueOf(process.isSlowQuery());
        }
        return null;
    }
    
    private Image getImage(String path) {
        Image image = imagesMap.get(path);
        if (image == null) {
            image = new Image(null, getClass().getClassLoader().getResourceAsStream(path));
            imagesMap.put(path, image);
        }
        return image;
    }
}
