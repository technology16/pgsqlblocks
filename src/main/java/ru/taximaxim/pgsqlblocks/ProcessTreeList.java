package ru.taximaxim.pgsqlblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

public class ProcessTreeList {
    private ConcurrentMap<Integer, Process> processMap;
    private List<Process> processList;
    public ProcessTreeList(ConcurrentMap<Integer,Process> processMap) {
        this.processMap = processMap;
        buildTree();
    }
    private void buildTree() {
        for(Entry<Integer, Process> map : getProcessMap().entrySet()) {
            int blockedBy = map.getValue().getBlockedBy();
            if(blockedBy != 0) {
                map.getValue().setParent(getProcessMap().get(blockedBy));
                getProcessMap().get(blockedBy).addChildren(map.getValue());
            }
        }
        for(Entry<Integer, Process> map : getProcessMap().entrySet()) {
            if(map.getValue().getBlockedBy() == 0) {
                getTreeList().add(map.getValue());
            }
        }
    }
    public ConcurrentMap<Integer, Process> getProcessMap() {
        return processMap;
    }
    public List<Process> getTreeList() {
        if (processList==null) {
            processList = new ArrayList<Process>();
        }
        return processList;
    }
}
