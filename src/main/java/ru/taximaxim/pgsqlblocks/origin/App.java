package ru.taximaxim.pgsqlblocks.origin;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import ru.taximaxim.pgsqlblocks.ui.origin.MainForm;

public final class App {
    
    private static final Logger LOG = Logger.getLogger(App.class);
    
    private static Display display;
    
    private App() {}
    
    public static void main( String[] args ) {
        try {
            display = new Display();
            MainForm.getInstance().show(display);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        } finally {
            display.dispose();
        }
    }
}
