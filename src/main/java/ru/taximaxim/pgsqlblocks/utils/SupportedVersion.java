package ru.taximaxim.pgsqlblocks.utils;


public enum SupportedVersion {
    VERSION_9_2 ("9.2"),
    VERSION_9_3 ("9.3"),
    VERSION_9_4 ("9.4"),
    VERSION_9_5 ("9.5"),
    VERSION_9_6 ("9.6"),
    VERSION_10 ("10.0");

    private final String version;

    SupportedVersion(String text) {
        this.version = text;
    }

    public String getVersion() {
        return version;
    }

}
