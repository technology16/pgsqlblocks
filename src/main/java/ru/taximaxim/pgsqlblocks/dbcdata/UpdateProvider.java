package ru.taximaxim.pgsqlblocks.dbcdata;

import ru.taximaxim.pgsqlblocks.IUpdateListener;

public abstract class UpdateProvider {
    private IUpdateListener listener;
    
    public final void setUpdateListener(IUpdateListener listener){
        this.listener = listener;
    }
    
    public final void notifyUpdated(){
        if(listener != null){
            listener.serverUpdated();
        }
    }
}
