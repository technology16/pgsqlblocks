package ru.taximaxim.pgsqlblocks.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX");

    private static final SimpleDateFormat dateFormatWithoutTimeZone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Date dateFromString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        Date result = null;
        try {
            result = dateFormat.parse(dateString);
        } catch (ParseException e) {
            System.out.println(e);
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

}
