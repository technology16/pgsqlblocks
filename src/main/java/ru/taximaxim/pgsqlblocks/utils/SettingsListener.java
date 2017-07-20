package ru.taximaxim.pgsqlblocks.utils;


public interface SettingsListener {


    void settingsUpdatePeriodChanged(int updatePeriod);
    void settingsShowIdleChanged(boolean isShowIdle);
    void settingsShowBackendPidChanged(boolean isShowBackendPid);
    void settingsAutoUpdateChanged(boolean isAutoUpdate);

}
