/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2018 "Technology" LLC
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

import java.util.*;

public enum SupportedVersion {
    VERSION_9_2 (90200, "9.2", "9.2"),
    VERSION_9_3 (90300, "9.3", "9.3"),
    VERSION_9_4 (90400, "9.4", "9.4"),
    VERSION_9_5 (90500, "9.5", "9.5"),
    VERSION_9_6 (90600, "9.6", "9.6"),
    VERSION_10 (100000, "10.0", "10.0"),
    VERSION_DEFAULT(100000, "10.0", "");

    private final String version;
    private final int versionNumber;
    private final String versionText;

    SupportedVersion(int versionNumber, String version, String versionText) {
        this.versionNumber = versionNumber;
        this.version = version;
        this.versionText = versionText;
    }

    public static List<SupportedVersion> getValuesNoDefault() {
        List<SupportedVersion> list = new ArrayList<>(Arrays.asList(SupportedVersion.values()));
        list.remove(SupportedVersion.VERSION_DEFAULT);
        return list;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionText() {
        return versionText;
    }

    public static Optional<SupportedVersion> getByVersionName(String text){
        return SupportedVersion.getValuesNoDefault().stream().filter(sv -> sv.getVersion().equals(text)).findAny();
    }

    public static Optional<SupportedVersion> getByVersionNumber(int number){
        List<SupportedVersion> versions = SupportedVersion.getValuesNoDefault();
        Collections.reverse(versions);
        return versions.stream().filter(sv -> sv.checkVersion(number)).findFirst();
    }

    private boolean checkVersion(int number) {
        return number >= this.versionNumber;
    }
}
