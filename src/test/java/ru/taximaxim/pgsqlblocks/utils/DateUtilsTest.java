package ru.taximaxim.pgsqlblocks.utils;

import org.junit.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {

    private static final int HOUR = 3600;
    private static final Map<Duration, String> durationToExpectedRepresentation =
            Collections.unmodifiableMap(new HashMap<Duration, String>() {{
                put(null, "");
                put(Duration.ZERO, "00:00:00");
                put(Duration.ofSeconds(0), "00:00:00");
                put(Duration.ofSeconds(6), "00:00:06");
                put(Duration.ofSeconds(60 + 1), "00:01:01");
                put(Duration.ofSeconds(60 + 13), "00:01:13");
                put(Duration.ofSeconds(300), "00:05:00");
                put(Duration.ofSeconds(300 + 5), "00:05:05");
                put(Duration.ofSeconds(HOUR), "01:00:00");
                put(Duration.ofSeconds(HOUR + 9), "01:00:09");
                put(Duration.ofSeconds(10 * HOUR), "10:00:00");
                put(Duration.ofSeconds(10 * HOUR + 305), "10:05:05");
                put(Duration.ofSeconds(30 * HOUR + 305), "30:05:05");
                put(Duration.ofSeconds(100 * HOUR + 305), "100:05:05");
            }});

    @Test
    public void durationToStringTest() {
        durationToExpectedRepresentation.forEach((d, r) -> assertEquals(r, DateUtils.durationToString(d)));
    }
}