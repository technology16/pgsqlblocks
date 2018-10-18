/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
 * %
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.utils;

import java.util.ResourceBundle;

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
    DECORATOR_BLOCKED,
    DECORATOR_UPDATE,
    CONN_DISABLED,
    CONN_CONNECTED,
    CONN_ERROR,
    CONN_UPDATE,
    PROC_WORKING,
    PROC_BLOCKING,
    PROC_BLOCKED,
    SHOW_LOG_PANEL,
    HIDE_LOG_PANEL,
    BLOCKS_JOURNAL_FOLDER,
    TABLE,
    FOLDER,
    DEFAULT;

    /**
     * Получение иконки
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
        case DECORATOR_BLOCKED:
            return "images/locker_8.png";
        case DECORATOR_UPDATE:
            return "images/update_8.png";
        case CONN_DISABLED:
            return "images/db_f_16.png";
        case CONN_CONNECTED:
            return "images/db_t_16.png";
        case CONN_ERROR:
            return "images/db_e_16.png";
        case CONN_UPDATE:
            return "images/on_update_16.png";
        case PROC_WORKING:
            return "images/nb_16.png";
        case PROC_BLOCKING:
            return "images/locker_16.png";
        case PROC_BLOCKED:
            return "images/locked_16.png";
        case SHOW_LOG_PANEL:
            return "images/log_show_16.png";
        case HIDE_LOG_PANEL:
            return "images/log_hide_16.png";
        case BLOCKS_JOURNAL_FOLDER:
            return "images/blocks_journal_folder_16.png";
        case FOLDER:
            return "images/folder_16.png";
        case TABLE:
            return "images/table_16.png";
        default:
            return "images/void_16.png";
        }
    }
    
    public String getDescription(ResourceBundle resources) {
        switch(this) {
        case ADD_DATABASE:
            return resources.getString("add_db");
        case DELETE_DATABASE:
            return resources.getString("delete_db");
        case EDIT_DATABASE:
            return resources.getString("edit_db");
        case CONNECT_DATABASE:
            return resources.getString("connect");
        case DISCONNECT_DATABASE:
            return resources.getString("disconnect");
        case UPDATE:
            return resources.getString("update");
        case AUTOUPDATE:
            return resources.getString("autoupdate");
        case VIEW_ONLY_BLOCKED:
            return resources.getString("view_only_blocked");
        case EXPORT_BLOCKS:
            return resources.getString("save_blocks");
        case IMPORT_BLOCKS:
            return resources.getString("open_blocks");
        case SETTINGS:
            return resources.getString("settings");
        case FILTER:
            return resources.getString("process_filter");
        case CANCEL_UPDATE:
            return resources.getString("cancel_update");
        case SHOW_LOG_PANEL:
            return resources.getString("show_logs_panel");
        case HIDE_LOG_PANEL:
            return resources.getString("hide_logs_panel");
        case BLOCKS_JOURNAL_FOLDER:
            return resources.getString("show_saved_blocks_journals");
        case TABLE:
            return resources.getString("columns");
        default:
            return resources.getString("default_action");
        }
    }
}
