package ru.taximaxim.pgSqlBlocks;

import org.eclipse.swt.widgets.Display;

import ru.taximaxim.psSqlBlocks.ui.MainForm;


public class App {
    
    private static Display display;
    
    private App() {}

    public static void main( String[] args ) {
        try {
            display = new Display();
            MainForm.getInstance().show(display);
        } catch (Exception e) {

        } finally {
            display.dispose();
        }
    }
}
