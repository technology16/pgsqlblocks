package ru.taximaxim.pgsqlblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessTreeList {
    private ConcurrentHashMap<Integer, Process> processMap;
    private List<Process> processList = new ArrayList<Process>();
    public ProcessTreeList(ConcurrentHashMap<Integer,Process> processMap) {
        this.processMap = processMap;
        buildTree();
    }
    
    private void buildTree() {
        for(Entry<Integer, Process> map : processMap.entrySet()) {
            int blockedBy = map.getValue().getBlockedBy();
            int blockingLocks = map.getValue().getBlockingLocks();

            if(blockedBy != 0) {
                map.getValue().setParent(processMap.get(blockedBy));
                processMap.get(blockedBy).addChildren(map.getValue());
            }
            if((blockingLocks != 0) & (blockingLocks != blockedBy)) {
                map.getValue().setParent(processMap.get(blockingLocks));
                processMap.get(blockingLocks).addChildren(map.getValue());
            }
        }
        for(Entry<Integer, Process> map : processMap.entrySet()) {
            if((map.getValue().getBlockedBy() == 0) & (map.getValue().getBlockingLocks() == 0) ) {
                processList.add(map.getValue());
            }
        }
    }
    public List<Process> getTreeList() {
        return processList;
    }
}
