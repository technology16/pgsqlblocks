package ru.taximaxim.pgsqlblocks.modules.db.model;

import ru.taximaxim.pgsqlblocks.utils.Images;

public enum DBStatus {
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
