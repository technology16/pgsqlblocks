package ru.taximaxim.pgsqlblocks;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import ru.taximaxim.pgsqlblocks.ui.MainForm;


public final class App {
    private static Logger log = Logger.getLogger(App.class);
    private static Display display;
    
    private App() {}

    public static void main( String[] args ) {
        try {
            display = new Display();
            MainForm.getInstance().show(display);
        } catch (Exception e) {
            log.error(e);
        } finally {
            display.dispose();
        }
    }
}
