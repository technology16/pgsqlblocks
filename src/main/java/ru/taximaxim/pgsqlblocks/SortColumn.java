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
    XACT_START,
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
                return "PID";
            case BLOCKED_COUNT:
                return "Блокирует";
            case APPLICATION_NAME:
                return "Приложение";
            case DATNAME:
                return "Имя БД";
            case USENAME:
                return "Роль";
            case CLIENT:
                return "Данные клиента";
            case BACKEND_START:
                return "Подключение к серверу";
            case QUERY_START:
                return "Старт запроса";
            case XACT_START:
                return "Старт транзакции";
            case STATE:
                return "Состояние";
            case STATE_CHANGE:
                return "Изменено";
            case BLOCKED:
                return "Кем блокируется";
            case LOCKTYPE:
                return "Тип объекта блокировки";
            case RELATION:
                return "Объект блокировки";
            case QUERY:
                return "Запрос";
            case SLOWQUERY:
                return "Долгий процесс";
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
            case STATE:
                return 55;
            case BLOCKED:
            case LOCKTYPE:
            case RELATION:
                return 70;
            case PID:
            case SLOWQUERY:
                return 80;
            case BLOCKED_COUNT:
            case DATNAME:
            case USENAME:
            case CLIENT:
                return 110;
            case BACKEND_START:
            case QUERY_START:
            case XACT_START:
            case STATE_CHANGE:
                return 145;
            case APPLICATION_NAME:
            case QUERY:
                return 150;
            default:
                return 100;
        }
    }
}
