package ru.taximaxim.pgsqlblocks;

import org.eclipse.swt.SWT;

public enum SortDirection {
    UP,
    DOWN;
    
    public SortDirection getOpposite() {
        if(this == UP) {
            return DOWN;
        } else {
            return UP;
        }
    }
    
    public int getSwtData(){
        if(this == UP) {
            return SWT.UP;
        } else {
            return SWT.DOWN;
        }
    }
}
