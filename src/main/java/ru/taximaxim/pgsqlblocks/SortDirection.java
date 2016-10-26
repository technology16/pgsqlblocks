package ru.taximaxim.pgsqlblocks;

import org.eclipse.swt.SWT;

public enum SortDirection {
    UP,
    DOWN;
    
    public SortDirection getOpposite() {
        return this == UP ? DOWN : UP;
    }
    
    public int getSwtData(){
        return this == UP ? SWT.UP : SWT.DOWN;
    }
}
