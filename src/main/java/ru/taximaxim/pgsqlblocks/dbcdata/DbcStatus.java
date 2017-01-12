package ru.taximaxim.pgsqlblocks.dbcdata;

import ru.taximaxim.pgsqlblocks.utils.Images;

public enum DbcStatus {
    DISABLED,
    CONNECTED,
    CONNECTION_ERROR,
    UPDATE;

    /**
     * Получение иконки в зависимости от состояния
     * @return
     */
    public Images getStatusImage() {
        switch(this) {
        case DISABLED:
            return Images.CONN_DISABLED;
        case CONNECTED:
            return Images.CONN_CONNECTED;
        case CONNECTION_ERROR:
            return Images.CONN_ERROR;
        case UPDATE:
            return Images.CONN_UPDATE;
        default:
            return Images.DEFAULT;
        }
    }
}
