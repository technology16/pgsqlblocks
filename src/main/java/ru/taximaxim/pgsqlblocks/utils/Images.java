package ru.taximaxim.pgsqlblocks.utils;

public enum Images {

    ADD_DATABASE,
    DELETE_DATABASE,
    EDIT_DATABASE,
    CONNECT_DATABASE,
    DISCONNECT_DATABASE,
    UPDATE,
    AUTOUPDATE,
    VIEW_ONLY_BLOCKED,
    EXPORT_BLOCKS,
    IMPORT_BLOCKS,
    SETTINGS,
    FILTER,
    CANCEL_UPDATE,
    BLOCKED,
    UNBLOCKED,
    LITTLE_BLOCKED,
    LITTLE_UPDATE;
    
    /**
     * Получение иконки
     * @return
     */
    public String getImageAddr() {
        switch(this) {
        case ADD_DATABASE:
            return "images/db_add_16.png";
        case DELETE_DATABASE:
            return "images/db_del_16.png";
        case EDIT_DATABASE:
            return "images/db_edit_16.png";
        case CONNECT_DATABASE:
            return "images/db_connect_16.png";
        case DISCONNECT_DATABASE:
            return "images/db_disconnect_16.png";
        case UPDATE:
            return "images/update_16.png";
        case AUTOUPDATE:
            return "images/autoupdate_16.png";
        case VIEW_ONLY_BLOCKED:
            return "images/db_ob_16.png";
        case EXPORT_BLOCKS:
            return "images/save_16.png";
        case IMPORT_BLOCKS:
            return "images/document_open_16.png";
        case SETTINGS:
            return "images/settings.png";
        case FILTER:
            return "images/filter.png";
        case CANCEL_UPDATE:
            return "images/cancel_update_16.png";
        case BLOCKED:
            return "images/block-16x16.gif";
        case UNBLOCKED:
            return "images/unblock-16x16.gif";
        case LITTLE_BLOCKED:
            return "images/locker_8.png";
        case LITTLE_UPDATE:
            return "images/update_8.png";
        default:
            return "images/void_16.png";
        }
    }
    
    public String getDescription() {
        switch(this) {
        case ADD_DATABASE:
            return "Добавить БД";
        case DELETE_DATABASE:
            return "Удалить БД";
        case EDIT_DATABASE:
            return "Редактировать БД";
        case CONNECT_DATABASE:
            return "Подключиться";
        case DISCONNECT_DATABASE:
            return "Отключиться";
        case UPDATE:
            return "Обновить";
        case AUTOUPDATE:
            return "Автообновление";
        case VIEW_ONLY_BLOCKED:
            return "Показывать только блокирующие и блокированные процессы";
        case EXPORT_BLOCKS:
            return "Выгрузить историю блокировок";
        case IMPORT_BLOCKS:
            return "Открыть файл с историей блокировок";
        case SETTINGS:
            return "Настройки";
        case FILTER:
            return "Фильтр";
        case CANCEL_UPDATE:
            return "Отменить обновление";
        default:
            return "Действие по-умолчанию";
        }
    }
}
