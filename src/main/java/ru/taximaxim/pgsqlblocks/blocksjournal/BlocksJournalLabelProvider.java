package ru.taximaxim.pgsqlblocks.blocksjournal;

import org.eclipse.swt.graphics.Image;
import ru.taximaxim.pgsqlblocks.SortColumn;
import ru.taximaxim.pgsqlblocks.TreeLabelProvider;
import ru.taximaxim.pgsqlblocks.process.Block;
import ru.taximaxim.pgsqlblocks.process.Process;

import java.util.Date;
import java.util.stream.Collectors;

public class BlocksJournalLabelProvider extends TreeLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        Process process;
        if (element instanceof BlocksJournalProcess) {
            BlocksJournalProcess journalProcess = (BlocksJournalProcess) element;
            process = journalProcess.getProcess();
        } else {
            process = (Process) element;
        }
        if (columnIndex == 0) {
            return getImage(process.getStatus().getStatusImage().getImageAddr());
        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        Process process;
        if (element instanceof BlocksJournalProcess) {
            BlocksJournalProcess journalProcess = (BlocksJournalProcess) element;
            switch (columnIndex) {
                case 0:
                    return journalProcess.getDbcName();
                case 1:
                    return journalProcess.getCreateDate().toString();
                case 2:
                    Date closeDate = journalProcess.getCloseDate();
                    return closeDate != null ? closeDate.toString() : "";
            }
            process = journalProcess.getProcess();
        } else {
            process = (Process) element;
        }
        switch (SortColumn.values()[columnIndex]) {
            case PID: return String.valueOf(process.getPid());
            case BLOCKED_COUNT: return String.valueOf(process.getChildren().size());
            case APPLICATION_NAME: return process.getCaller().getApplicationName();
            case DATNAME: return process.getCaller().getDatname();
            case USENAME: return process.getCaller().getUsername();
            case CLIENT: return process.getCaller().getClient();
            case BACKEND_START: return process.getQuery().getBackendStart();
            case QUERY_START: return process.getQuery().getQueryStart();
            case XACT_START: return process.getQuery().getXactStart();
            case STATE: return process.getState();
            case STATE_CHANGE: return process.getStateChange();
            case BLOCKED: return process.getBlocks().stream()
                    .map(b -> String.valueOf(b.getBlockingPid()))
                    .collect(Collectors.joining(","));
            case LOCKTYPE: return process.getBlocks().stream()
                    .map(Block::getLocktype)
                    .distinct()
                    .collect(Collectors.joining(","));
            case RELATION: return process.getBlocks().stream()
                    .map(Block::getRelation)
                    .filter(r -> r != null && !r.isEmpty())
                    .distinct()
                    .collect(Collectors.joining(","));
            case QUERY: return process.getQuery().getQueryString();
            case SLOWQUERY: return String.valueOf(process.getQuery().isSlowQuery());
            default: return null;
        }
    }

    private Image getImage(String path) {
        return imagesMap.computeIfAbsent(path, k ->
                new Image(null, getClass().getClassLoader().getResourceAsStream(path)));
    }
}
