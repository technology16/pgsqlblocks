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
            case XACT_START:
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
