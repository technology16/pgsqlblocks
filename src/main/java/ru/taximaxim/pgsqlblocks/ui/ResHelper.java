package ru.taximaxim.pgsqlblocks.ui;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Вспомогательный класс для получения ресурсов изображений
 * 
 * @author ismagilov_mg
 */
public class ResHelper {
    
    private static volatile ResHelper resHelper;
    
    protected static final Logger LOG = Logger.getLogger(ResHelper.class);
    
    public static ResHelper getInstance() {
        ResHelper localResHelper = resHelper;
        if(localResHelper == null) {
            synchronized (ResHelper.class) {
                localResHelper = resHelper;
                if(localResHelper == null) {
                    resHelper = localResHelper = new ResHelper();
                }
            }
        }
        return localResHelper;
    }
    public synchronized Image setImage(Composite composite,String addr) {
        try {
            return new Image(composite.getDisplay(),composite.getClass().getClassLoader().getResourceAsStream(addr));
        } catch(Exception e) {
            LOG.error("Ресурс не найден :" + addr, e);
        }
        return null;
    }
}
