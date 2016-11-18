package ru.taximaxim.pgsqlblocks.process;

public enum ProcessStatus {
    WORKING,
    BLOCKING,
    BLOCKED;
    
    /**
     * Получение иконки в зависимости от состояния
     * @return
     */
    public String getImageAddr() {
        switch(this) {
        case WORKING:
            return "images/nb_16.png";
        case BLOCKING:
            return "images/locker_16.png";
        case BLOCKED:
            return "images/locked_16.png";
        default:
            return "images/void_16.png";
        }
    }
}
