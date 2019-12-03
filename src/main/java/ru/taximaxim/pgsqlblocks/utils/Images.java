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

public enum Images {

    ADD_DATABASE("images/db_add_16.png"),
    DELETE_DATABASE("images/db_del_16.png"),
    EDIT_DATABASE("images/db_edit_16.png"),
    CONNECT_DATABASE("images/db_connect_16.png"),
    DISCONNECT_DATABASE("images/db_disconnect_16.png"),
    UPDATE("images/update_16.png"),
    AUTOUPDATE("images/autoupdate_16.png"),
    VIEW_ONLY_BLOCKED("images/db_ob_16.png"),
    EXPORT_BLOCKS("images/save_16.png"),
    IMPORT_BLOCKS("images/document_open_16.png"),
    SETTINGS("images/settings.png"),
    FILTER("images/filter.png"),
    CANCEL_UPDATE("images/cancel_update_16.png"),
    BLOCKED("images/block-16x16.gif"),
    UNBLOCKED("images/unblock-16x16.gif"),
    DECORATOR_BLOCKED("images/locker_8.png"),
    DECORATOR_UPDATE("images/update_8.png"),
    CONN_DISABLED("images/db_f_16.png"),
    CONN_CONNECTED("images/db_t_16.png"),
    CONN_ERROR("images/db_e_16.png"),
    CONN_UPDATE("images/on_update_16.png"),
    PROC_WORKING("images/nb_16.png"),
    PROC_BLOCKING("images/locker_16.png"),
    PROC_BLOCKED("images/blocked_16.png"),
    SHOW_LOG_PANEL("images/log_show_16.png"),
    HIDE_LOG_PANEL("images/log_hide_16.png"),
    BLOCKS_JOURNAL_FOLDER("images/blocks_journal_folder_16.png"),
    FOLDER("images/folder_16.png"),
    TABLE("images/table_16.png");

    private String location;

    private Images(String location) {
        this.location = location;
    }

    public String getImageAddr() {
        return location;
    }
}
