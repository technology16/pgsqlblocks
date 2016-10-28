package ru.taximaxim.pgsqlblocks.dbcdata;

public enum DbcStatus {
    DISABLED,
    CONNECTED,
    CONNECTION_ERROR,
    UPDATE;

    /**
     * Получение иконки в зависимости от состояния
     * @return
     */
    public String getImageAddr() {
        switch(this) {
        case DISABLED:
            return "images/db_f_16.png";
        case CONNECTED:
            return "images/db_t_16.png";
        case CONNECTION_ERROR:
            return "images/db_e_16.png";
        case UPDATE:
            return "images/on_update_16.png";
        default:
            return "images/void_16.png";
        }
    }
}
