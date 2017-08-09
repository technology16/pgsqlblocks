package ru.taximaxim.pgsqlblocks.utils;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public final class DateUtils {

    private static final Logger LOG = Logger.getLogger(DateUtils.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX");

    private static final SimpleDateFormat dateFormatWithoutTimeZone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Date dateFromString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        Date result = null;
        try {
            result = dateFormat.parse(dateString);
        } catch (ParseException exception) {
            LOG.error(exception.getMessage(), exception);
        }
        return result;
    }

    public static String dateToString(Date date) {
        if (date == null) {
            return "";
        }
        return dateFormatWithoutTimeZone.format(date);
    }

    public static String dateToStringWithTz(Date date) {
        if (date == null) {
            return "";
        }
        return dateFormat.format(date);
    }

    public static int compareDates(Date d1, Date d2) {
        if (d1 == null) {
            return d2 == null ? 0 : -1;
        } else {
            return d2 == null ? 1 : d1.compareTo(d2);
        }
    }

    public static int compareDurations(Duration d1, Duration d2) {
        if (d1 == null) {
            return d2 == null ? 0 : -1;
        } else {
            return d2 == null ? 1 : d1.compareTo(d2);
        }
    }

    public static String durationToString(Duration duration) {
        if (duration == null) {
            return "";
        }
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%02d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

}
