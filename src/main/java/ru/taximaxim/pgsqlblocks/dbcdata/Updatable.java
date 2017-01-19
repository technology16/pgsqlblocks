package ru.taximaxim.pgsqlblocks.dbcdata;

public interface Updatable {

    void startUpdater();

    //void startUpdaterOnce();

    void stopUpdater();
}