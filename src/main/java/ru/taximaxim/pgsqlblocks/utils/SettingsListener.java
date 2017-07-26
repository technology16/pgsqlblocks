package ru.taximaxim.pgsqlblocks.utils;


public interface SettingsListener {


    void settingsUpdatePeriodChanged(int updatePeriod);
    void settingsShowIdleChanged(boolean isShowIdle);
    void settingsAutoUpdateChanged(boolean isAutoUpdate);

}
