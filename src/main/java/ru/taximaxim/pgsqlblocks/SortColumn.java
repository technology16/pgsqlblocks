package ru.taximaxim.pgsqlblocks;

public enum SortColumn {
    PID,
    BLOCKED_COUNT,
    APPLICATION_NAME,
    DATNAME,
    USENAME,
    CLIENT,
    BACKEND_START,
    QUERY_START,
    XACT_STAT,
    STATE,
    STATE_CHANGE,
    BLOCKED,
    LOCKTYPE,
    RELATION,
    QUERY,
    SLOWQUERY;

    /**
     * Получение имени колонки
     * @return String
     */
    public String getLowCaseName() {
        // TODO: переделать в switch с русскими названиями
        return this.toString().toLowerCase();
    }
    /**
     * Получение размера колонки
     * @return int
     */
    public int getColSize() {
        switch (this) {
            case PID:
                return 80;
            case BLOCKED_COUNT:
                return 110;
            case APPLICATION_NAME:
                return 150;
            case DATNAME:
                return 110;
            case USENAME:
                return 110;
            case CLIENT:
                return 110;
            case BACKEND_START:
                return 145;
            case QUERY_START:
                return 145;
            case XACT_STAT:
                return 145;
            case STATE:
                return 55;
            case STATE_CHANGE:
                return 145;
            case BLOCKED:
                return 70;
            case LOCKTYPE:
                return 70;
            case RELATION:
                return 70;
            case QUERY:
                return 150;
            case SLOWQUERY:
                return 80;
            default:
                return 100;
        }
    }
}
