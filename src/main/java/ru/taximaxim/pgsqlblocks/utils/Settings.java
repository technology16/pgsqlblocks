package ru.taximaxim.pgsqlblocks.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Класс для работы с настройка пользователя
 * @author petrov_im
 *
 */
public final class Settings {

    private static final Logger LOG = Logger.getLogger(Settings.class);

    private static final String UPDATE_PERIOD = "update_period";
    private static final String LOGIN_TIMEOUT = "login_timeout";
    private static final String AUTO_UPDATE = "auto_update";
    private static final String ONLY_BLOCKED = "only_blocked";
    private static final String SHOW_IDLE = "show_idle";
    private static final String SHOW_TOOL_TIP = "show_tool_tip";
    private static final String SHOW_BACKEND_PID = "show_backend_pid";
    private static final String CONFIRM_REQUIRED = "confirm_required";

    private int updatePeriod;
    private int loginTimeout;

    private boolean autoUpdate;

    private boolean onlyBlocked;
    private boolean showIdle;
    private boolean showToolTip;
    private boolean confirmRequired;

    private boolean showBackendPid;
    private Properties properties;

    private File propFile;
    private static Settings instance;

    private Settings() {
        Properties defaults = new Properties();
        defaults.put(UPDATE_PERIOD, "10");
        defaults.put(AUTO_UPDATE, "true");
        defaults.put(ONLY_BLOCKED, "false");
        defaults.put(LOGIN_TIMEOUT, "10");
        defaults.put(SHOW_IDLE, "true");
        defaults.put(SHOW_TOOL_TIP, "false");
        defaults.put(SHOW_BACKEND_PID, "true");
        defaults.put(CONFIRM_REQUIRED, "true");

        properties = new Properties(defaults);
        propFile = PathBuilder.getInstance().getPropertiesPath().toFile();
        try (FileInputStream loadProp = new FileInputStream(propFile)) {
            properties.load(loadProp);
        } catch (FileNotFoundException e) {
            LOG.error(String.format("Файл %s не найден!", propFile.toString()));
        } catch (IOException e) {
            LOG.error(String.format("Ошибка чтения файла %s!", propFile.toString()));
        }

        this.updatePeriod = Integer.parseInt(properties.getProperty(UPDATE_PERIOD));
        this.autoUpdate = Boolean.parseBoolean(properties.getProperty(AUTO_UPDATE));
        this.onlyBlocked = Boolean.parseBoolean(properties.getProperty(ONLY_BLOCKED));
        this.loginTimeout = Integer.parseInt(properties.getProperty(LOGIN_TIMEOUT));
        this.showIdle = Boolean.parseBoolean(properties.getProperty(SHOW_IDLE));
        this.showToolTip = Boolean.parseBoolean(properties.getProperty(SHOW_TOOL_TIP));
        this.showBackendPid = Boolean.parseBoolean(properties.getProperty(SHOW_BACKEND_PID));
        this.confirmRequired = Boolean.parseBoolean(properties.getProperty(CONFIRM_REQUIRED));
    }

    public static Settings getInstance() {
        if(instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * Устанавливаем период обновления
     * @param updatePeriod
     */
    public void setUpdatePeriod(int updatePeriod) {
        this.updatePeriod = updatePeriod;
        saveProperties(UPDATE_PERIOD, Integer.toString(updatePeriod));
    }

    /**
     * Получаем период обновления
     * @return the updatePeriod
     */
    public int getUpdatePeriod() {
        return updatePeriod;
    }

    /**
     * Sets the maximum time in seconds that a driver will wait
     * while attempting to connect to a database once the driver has
     * been identified.
     *
     * @param seconds the login time limit in seconds; zero means there is no limit
     * @see #getLoginTimeout
     */
    public void setLoginTimeout(int seconds) {
        this.loginTimeout = seconds;
        saveProperties(LOGIN_TIMEOUT, Integer.toString(seconds));
    }
    /**
     * Gets the maximum time in seconds that a driver can wait
     * when attempting to log in to a database.
     *
     * @return the driver login time limit in seconds
     * @see #setLoginTimeout
     */
    public int getLoginTimeout() {
        return loginTimeout;
    }
    /**
     * Получаем флаг автообновления
     * @return the autoUpdate
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Устанавливаем флаг автообновления
     * @param autoUpdate the autoUpdate to set
     */
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        saveProperties(AUTO_UPDATE, Boolean.toString(autoUpdate));
    }

    /**
     * Получаем флаг отображения только блокированных процессов
     * @return the onlyBlocked
     */
    public boolean isOnlyBlocked() {
        return onlyBlocked;
    }

    /**
     * Устанавливаем флаг отображения тоьлко блокированных процессов
     * @param onlyBlocked the onlyBlocked to set
     */
    public void setOnlyBlocked(boolean onlyBlocked) {
        this.onlyBlocked = onlyBlocked;
        saveProperties(ONLY_BLOCKED, Boolean.toString(onlyBlocked));
    }

    /**
     * Устанавливаем флаг отображения бездействующих процессов
     * @param showIdle the showIdle to set
     */
    public void setShowIdle(boolean showIdle) {
        this.showIdle = showIdle;
        saveProperties(SHOW_IDLE, Boolean.toString(showIdle));
    }

    /**
     * Получаем флаг отображения бездействующих процессов
     * @return the showIdle
     */
    public boolean getShowIdle() {
        return showIdle;
    }

    /**
     * Устанавливаем флаг отображения запросов приложения среди процессов
     * @param showBackendPid the showIdle to set
     */
    public void setShowBackendPid(boolean showBackendPid) {
        this.showBackendPid = showBackendPid;
        saveProperties(SHOW_BACKEND_PID, Boolean.toString(showBackendPid));
    }

    /**
     * Получаем флаг отображения запросов приложения среди процессов
     * @return the showIdle
     */
    public boolean getShowBackendPid() {
        return showBackendPid;
    }

    /**
     * Устанавливаем флаг отображения уведомлений в трее
     * @param showToolTip the showIdle to set
     */
    public void setShowToolTip(boolean showToolTip) {
        this.showToolTip = showToolTip;
        saveProperties(SHOW_TOOL_TIP, Boolean.toString(showToolTip));
    }

    /**
     * Получаем флаг отображения уведомлений в трее
     * @return the showIdle
     */
    public boolean getShowToolTip() {
        return showToolTip;
    }

    /**
     * Получаем флаг "подтверждать действие отмены/уничтожения процесса"
     * @return флаг "подтверждать действие отмены/уничтожения процесса"
     * @see #setConfirmRequired(boolean)
     */
    public boolean isConfirmRequired() {
        return confirmRequired;
    }

    /**
     * Устанавливаем флаг "подтверждать действие отмены/уничтожения процесса"
     * @param confirmRequired флаг "подтверждать действие отмены/уничтожения процесса"
     * @see #isConfirmRequired()
     */
    public void setConfirmRequired(boolean confirmRequired) {
        this.confirmRequired = confirmRequired;
    }

    private void saveProperties(String key, String value) {
        try (FileOutputStream saveProp = new FileOutputStream(propFile)) {
            properties.setProperty(key, value);
            properties.store(saveProp, String.format("Save properties %s", key));
        } catch (FileNotFoundException e) {
            LOG.error(String.format("Файл %s не найден!", propFile.toString()));
        } catch (IOException e) {
            LOG.error(String.format("Ошибка записи в файл %s!", propFile.toString()));
        }
    }
}
