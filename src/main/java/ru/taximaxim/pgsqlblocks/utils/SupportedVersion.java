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
