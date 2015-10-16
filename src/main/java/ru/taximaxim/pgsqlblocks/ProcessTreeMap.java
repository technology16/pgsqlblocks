package ru.taximaxim.pgsqlblocks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProcessTreeMap {
    
    private static ProcessTreeMap instanse = new ProcessTreeMap();
    
    private ConcurrentMap<DbcData, ProcessTree> processTreeMap = new ConcurrentHashMap<>();
    
    public static ProcessTreeMap getInstanse() {
        return instanse;
    }
    
    public ProcessTree getProcessTree(DbcData dbcData) {
        ProcessTree processTree = processTreeMap.get(dbcData);
        if(processTree == null){
            processTree = new ProcessTree(dbcData);
            processTreeMap.put(dbcData, processTree);
        }
        
        return processTree;
    }

}
