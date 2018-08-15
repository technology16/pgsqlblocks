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
package ru.taximaxim.pgsqlblocks.l10n;

import java.util.ListResourceBundle;

public class PgSqlBlocks_en extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {"confirm_action", "Confirm action"},
                {"exit_confirm_message", "Do you really want to exit pgSqlBlocks?"},
                {"delete_confirm_message", "Do you really want to delete %s?"},
                {"kill_process_confirm_message", "Do you really want to kill the process %s?"},
                {"cancel_process_confirm_message", "Do you really want to send a process cancellation signal %s?"},
                {"cancel_process_cancelled_message", "Cancelling of processes was cancelled"},
                {"cancel_process_error_message", "Cancelling of processes was finished with error %s"},
                {"kill_process_cancelled_message", "Termination of processes was cancelled"},
                {"kill_process_error_message", "Termination of processes was finished with error %s"},
                {"system_tray_not_available_message", "The system tray is not available"},
                {"about", "&About"},
                {"exit", "&Exit"},
                {"current_activity", "Current Activity"},
                {"database", "Database"},
                {"blocks_journal", "Blocks journal"},
                {"kill_process", "Kill process(es)"},
                {"cancel_process", "Cancel process(es)"},
                {"lock_history", "Lock history"},
                {"lock_saved", "Lock saved..."},
                {"no_locks", "No locks were found to save"},
                {"view_lock_history", "View lock history"},
                {"one_db_has_lock", "In one database there is a lock"},
                {"db_has_lock", "In %s there is a lock"},
                {"several_db_has_lock", "Several databases have a lock"},
                {"error_reading_the_manifest", "Error reading the manifest"},
                {"process_terminated", "%s  pid=%s is terminated."},
                {"process_not_terminated", "%s failed to terminate pid=%s: %s"},
                {"process_cancelled", "%s  pid=%s is cancelled."},
                {"process_not_cancelled", "%s failed to cancel pid=%s: %s"},
                {"remove_dbcdata_on_disconnect", "Remove dbcData on disconnect \"%s\" from updaterList"},
                {"db_already_connected", "%s already connected."},
                {"db_connecting", "Connecting %s..."},
                {"db_connected", "%s Connection created."},
                {"db_disconnected", "%s is disconnected."},
                {"db_disconnected_will_reconnect", "%s is disconnected. Will reconnect in %s seconds"},
                {"error_on_check_is_connected", "An error occurred while trying to get the value of \"isConnected \": %s"},
                {"db_exists_in_conf_file", "This database already exists in the configuration file"},
                {"db_update_error", "Error on DbcData: %s."},
                {"db_updating", "Updating \"%s\"..."},
                {"db_finish_updating", "Finish updating \"%s\"..."},
                {"db_error_on_connect", "Error on connect or update DbcData: %s"},
                {"include_blocked_processes", "Include blocked/blocking processes"},
                {"journals", "Journals"},
                {"refresh_files_list", "Refresh list of files"},
                {"show_saved_blocks_journals", "Show saved blocks journals"},
                {"saved_blocks_journals", "Saved blocks journals"},
                {"open_dir", "Open directory"},
                {"duration", "Duration"},

                // action icons
                {"add_db", "Add database"},
                {"delete_db", "Remove database"},
                {"edit_db", "Edit database"},
                {"connect", "Connect"},
                {"disconnect", "Disconnect"},
                {"update", "Update"},
                {"autoupdate", "Auto update"},
                {"view_only_blocked", "Show only blocking and blocked processes"},
                {"save_blocks", "Save blocks to file"},
                {"open_blocks", "Open blocks from file"},
                {"settings", "Settings"},
                {"process_filter", "Process filter"},
                {"cancel_update", "Stop update"},
                {"show_logs_panel", "Hide logs panel"},
                {"hide_logs_panel", "Show logs panel"},
                {"default_action", "Default"},

                // columns
                {"pid", "PID"},
                {"backend_type", "Type"},
                {"num_of_blocked_processes", "# blocked by"},
                {"application", "Application"},
                {"db_name", "Database"},
                {"user_name", "Username"},
                {"client", "Client"},
                {"backend_start", "Backend start"},
                {"query_start", "Query start"},
                {"xact_start", "Xact start"},
                {"state", "State"},
                {"state_change", "State change"},
                {"blocked_by", "Blocked by"},
                {"lock_type", "Lock type"},
                {"relation", "Relation"},
                {"query", "Query"},
                {"slow_query", "Slow query"},
                {"undefined", "Undefined"},
                {"block_start_date", "Block started"},
                {"block_end_date", "Block ended"},

                // settings dialog
                {"processes", "Processes"},
                {"auto_update_interval", "Autoupdate interval"},
                {"show_idle_process", "Show idle processes"},
                {"show_pgSqlBlock_process", "Show pgSqlBlock process"},
                {"notifications", "Notifications"},
                {"show_tray_notifications", "Show tray notifications"},
                {"prompt_confirmation_on_process_kill", "Prompt confirmation on process cancel/kill"},
                {"prompt_confirmation_on_program_close", "Prompt confirmation on program close"},
                {"columns", "Columns"},
                {"general", "General"},
                {"select_ui_language", "UI language (requires restart)"},

                // create database dialog
                {"name", "Connection name*"},
                {"host", "Host*"},
                {"port", "Port*"},
                {"version", "Version*"},
                {"user", "User*"},
                {"password", "Password"},
                {"use_pgpass_file", "Password will be stored in insecure storage. Use .pgpass file instead."},
                {"database_name", "Database*"},
                {"connect_automatically", "Auto connect"},
                {"add_new_connection", "Add new connection"},
                {"edit_connection", "Edit connection"},
                {"missing_connection_name", "Missing required field: connection name!"},
                {"already_exists", "Connection %s already exists!"},
                {"missing_host_port", "Missing required field: host and/or port!"},
                {"missing_database_user", "Missing required field: database and/or user!"},
                {"missing_database_version", "Missing required field: database version!"},
                {"attention", "Attention!"},

                //create update version dialog
                {"warning_title","Changes"},
                {"warning_text","Need to change connection's config file. Do it automatically?"}
        };
    }
}
