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
    public String getName() {
        switch (this) {
            case PID:
                return "Идентификатор процесса";
            case BLOCKED_COUNT:
                return "Кол-во заблокированных процессов";
            case APPLICATION_NAME:
                return "Имя при подключении/установке сессии";
            case DATNAME:
                return "Имя базы данных при подключении";
            case USENAME:
                return "Имя роли при подключении";
            case CLIENT:
                return "Данные клиента";
            case BACKEND_START:
                return "Время подключения к серверу";
            case QUERY_START:
                return "Время старта запроса";
            case XACT_STAT:
                return "Время старта транзакции";
            case STATE:
                return "Состояние процесса";
            case STATE_CHANGE:
                return "Время последнего изменения состояния процесса";
            case BLOCKED:
                return "Кем блокируется";
            case LOCKTYPE:
                return "Тип блокируемого объекта";
            case RELATION:
                return "OID отношения, являющегося целью блокировки";
            case QUERY:
                return "Текст запроса";
            case SLOWQUERY:
                return "\"Долгий\" процесс";
            default:
                return "Без имени";
        }
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
