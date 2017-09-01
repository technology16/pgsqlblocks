package ru.taximaxim.pgsqlblocks.common.models;

import ru.taximaxim.pgsqlblocks.utils.Images;

public enum DBProcessStatus {
    WORKING("Working"),
    BLOCKING("Blocking"),
    BLOCKED("Blocked");

    private final String descr;

    DBProcessStatus(String descr) {
        this.descr = descr;
    }

    public String getDescr() {
        return descr;
    }

    public static DBProcessStatus getInstanceForDescr(String descr) {
        if (descr == null || descr.isEmpty())
            return WORKING;
        switch (descr) {
            case "Working": return WORKING;
            case "Blocking": return BLOCKING;
            case "Blocked": return BLOCKED;
            default: return WORKING;
        }
    }

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
