/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.utils;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

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
