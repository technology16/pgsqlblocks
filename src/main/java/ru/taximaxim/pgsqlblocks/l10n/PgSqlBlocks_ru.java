/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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

public class PgSqlBlocks_ru extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {"confirm_action", "Подтверждение действия"},
                {"exit_confirm_message", "Вы действительно хотите выйти из pgSqlBlocks?"},
                {"delete_confirm_message", "Вы действительно хотите удалить {0}?"},
                {"kill_process_confirm_message", "Вы действительно хотите уничтожить процесс {0}?"},
                {"cancel_process_confirm_message", "Вы действительно хотите послать сигнал отмены процесса {0}?"},
                {"system_tray_not_available_message", "Системный трей недоступен"},
                {"about", "&О приложении"},
                {"exit", "&Выход"},
                {"current_activity", "Текущая активность"},
                {"blocks_journal", "Журнал блокировок"},
                {"database", "Базы данных"},
                {"kill_process", "Уничтожить процесс"},
                {"cancel_process", "Отменить процесс"},
                {"lock_history", "История блокировок"},
                {"lock_saved", "Блокировка сохранена..."},
                {"no_locks", "Не найдено блокировок для сохранения"},
                {"view_lock_history", "Открыть историю блокировок"},
                {"one_db_has_lock", "В одной имеется блокировка"},
                {"db_has_lock", "В {0} имеется блокировк"},
                {"several_db_has_lock", "В нескольких имеется блокировка"},
                {"error_reading_the_manifest", "Ошибка при чтении манифеста"},
                {"process_terminated", "{0}  pid={1} остановлен."},
                {"process_not_terminated", "{0}  pid={1} не удалось остановить."},
                {"process_cancelled", "{0}  pid={1} отменен."},
                {"process_not_cancelled", "{0}  pid={1} не удалось отменить."},
                {"remove_dbcdata_on_disconnect", "Удалить dbcData при отключении \"{0}\" из списка updaterList"},
                {"db_already_connected", "{0} соединение уже создано."},
                {"db_connecting", "Соединение {0}..."},
                {"db_connected", "{0} Соединение создано."},
                {"db_disconnected", "{0} Соединение закрыто."},
                {"db_disconnected_will_reconnect", "{0} Соединение закрыто. Переподключение произойдет через {1} секунд"},
                {"error_on_check_is_connected", "Ошибка при попытке получения значения \"isConnected\": {0}"},
                {"db_exists_in_conf_file", "Данное БД уже есть в конфигурационном файле"},
                {"db_update_error", "Ошибка в DbcData: {0}."},
                {"db_updating", "Обновление \"{0}\"..."},
                {"db_finish_updating", "Обновление завершено \"{0}\"."},
                {"db_error_on_connect", "Ошибка при обновлении DbcData: {0}"},
                {"include_blocked_processes", "Учитывать блокирующие/блокированные процессы"},
                {"journals", "Журналы"},
                {"refresh_files_list", "Обновить список файлов"},
                {"show_saved_blocks_journals", "Показать сохраненные журналы блокировок"},
                {"saved_blocks_journals", "Сохраненные журналы блокировок"},
                {"open_dir", "Открыть директорию"},
                {"duration", "Длительность"},

                // action icons
                {"add_db", "Добавить БД"},
                {"delete_db", "Удалить БД"},
                {"edit_db", "Редактировать БД"},
                {"connect", "Подключиться"},
                {"disconnect", "Отключиться"},
                {"update", "Обновить"},
                {"autoupdate", "Автообновление"},
                {"view_only_blocked", "Показывать только блокирующие и блокированные процессы"},
                {"save_blocks", "Выгрузить историю блокировок"},
                {"open_blocks", "Открыть файл с историей блокировок"},
                {"settings", "Настройки"},
                {"process_filter", "Фильтр"},
                {"cancel_update", "Отменить обновление"},
                {"show_logs_panel", "Скрыть панель логов"},
                {"hide_logs_panel", "Отображать панель логов"},
                {"default_action", "Действие по-умолчанию"},

                // columns
                {"pid", "PID"},
                {"num_of_blocked_processes", "Блокирует"},
                {"application", "Приложение"},
                {"db_name", "Имя БД"},
                {"user_name", "Роль"},
                {"client", "Данные клиента"},
                {"backend_start", "Подключение к серверу"},
                {"query_start", "Старт запроса"},
                {"xact_start", "Старт транзакции"},
                {"state", "Состояние"},
                {"state_change", "Изменено"},
                {"blocked_by", "Кем блокируется"},
                {"lock_type", "Тип объекта блокировки"},
                {"relation", "Объект блокировки"},
                {"query", "Запрос"},
                {"slow_query", "Долгий процесс"},
                {"undefined", "Без имени"},
                {"block_start_date", "Блокировка началась"},
                {"block_end_date", "Блокировка завершена"},

                // settings dialog
                {"processes", "Процессы"},
                {"auto_update_interval", "Период автообновления"},
                {"show_idle_process", "Показывать idle процессы"},
                {"show_pgSqlBlock_process", "Показывать собственные запросы среди процессов"},
                {"notifications", "Уведомления"},
                {"show_tray_notifications", "Показывать оповещения о блокировках в трее"},
                {"prompt_confirmation_on_process_kill", "Подтверждать отмену/уничтожение процесса"},
                {"prompt_confirmation_on_program_close", "Подтверждать при выходе из pgSqlBlocks"},
                {"columns", "Колонки"},
                {"general", "Общие"},
                {"select_ui_language", "Язык интерфейса (требует перезапуска)"},

                // create database dialog
                {"name", "Имя соединения*"},
                {"host", "Хост*"},
                {"port", "Порт*"},
                {"user", "Имя пользователя*"},
                {"password", "Пароль"},
                {"use_pgpass_file", "Указание пароля здесь небезопасно. Используйте .pgpass файл."},
                {"database_name", "Имя БД*"},
                {"connect_automatically", "Подкл. автоматически"},
                {"add_new_connection", "Добавить новое соединение"},
                {"edit_connection", "Редактировать соединение"},
                {"missing_connection_name", "Не заполнено обязательное поле: Имя соединения!"},
                {"already_exists", "Сервер с таким именем {0} уже существует!"},
                {"missing_host_port", "Не заполнены обязательные поля: Хост и/или Порт!"},
                {"missing_database_user", "Не заполнены обязательные поля: Имя БД и/или Имя пользователя!"},
                {"attention", "Внимание!"}
        };
    }
}
