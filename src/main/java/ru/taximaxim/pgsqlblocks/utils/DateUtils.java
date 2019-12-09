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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class DateUtils {

    private static final DateTimeFormatter FILE_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH''mm''ss");

    private static final DateTimeFormatter DATE_WITH_TZ =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");

    private static final DateTimeFormatter DATE_WITHOUT_TZ =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static Date dateFromString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        return Date.from(Instant.from(DATE_WITH_TZ.parse(dateString)));
    }

    public static String dateToString(Date date) {
        if (date == null) {
            return "";
        }

        return DATE_WITHOUT_TZ.format(date.toInstant());
    }

    public static String dateToStringWithTz(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_WITH_TZ.format(date.toInstant());
    }

    public static String dateToString(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        return FILE_DATE.format(date);
    }

    public static int compareDates(Date d1, Date d2) {
        if (d1 == null) {
            return d2 == null ? 0 : -1;
        } else {
            return d2 == null ? 1 : d1.compareTo(d2);
        }
    }

    public static String durationToString(Duration duration) {
        if (duration == null) {
            return "";
        } else {
            long seconds = duration.getSeconds();
            long absSeconds = Math.abs(seconds);
            String positive = String.format("%02d:%02d:%02d", absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60);
            return seconds < 0 ? "-" + positive : positive;
        }
    }

    private DateUtils() {}
}
