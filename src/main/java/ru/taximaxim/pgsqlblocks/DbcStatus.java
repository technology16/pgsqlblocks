package ru.taximaxim.pgsqlblocks;

public enum DbcStatus {
    DISABLED,
    CONNECTED,
    ERROR,
    BLOCKED;
    public String getImageAddr() {
        switch(this) {
        case DISABLED:
            return "images/db_f_16.png";
        case CONNECTED:
            return "images/db_t_16.png";
        case ERROR:
            return "images/db_e_16.png";
        case BLOCKED:
            return "images/locked_16.png";
        default:
            return "images/void_16.png";
        }
    }
}
