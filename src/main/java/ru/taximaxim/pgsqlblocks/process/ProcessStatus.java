package ru.taximaxim.pgsqlblocks.process;

import ru.taximaxim.pgsqlblocks.utils.Images;

public enum ProcessStatus {
    WORKING,
    BLOCKING,
    BLOCKED;
    
    /**
     * Получение иконки в зависимости от состояния
     * @return
     */
    public Images getStatusImage() {
        switch(this) {
        case WORKING:
            return Images.PROC_WORKING;
        case BLOCKING:
            return Images.PROC_BLOCKING;
        case BLOCKED:
            return Images.PROC_BLOCKED;
        default:
            return Images.DEFAULT;
        }
    }
}
