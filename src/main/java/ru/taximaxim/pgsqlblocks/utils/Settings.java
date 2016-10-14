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
    private static final String UPDATE_UI_PERIOD = "update_UI_period";
    private static final String LOGIN_TIMEOUT = "login_timeout";
    private static final String AUTO_UPDATE = "auto_update";
    private static final String ONLY_BLOCKED = "only_blocked";

    private int updatePeriod;
    private int updateUIPeriod;
    private int loginTimeout;

    private boolean autoUpdate;

    private boolean onlyBlocked;

    private Properties properties;
    private File propFile;

    private static Settings instance;

    private Settings() {
        properties = new Properties();
        propFile = PathBuilder.getInstance().getPropertiesPath().toFile();
        try (FileInputStream loadProp = new FileInputStream(propFile)) {
            properties.load(loadProp);
        } catch (FileNotFoundException e) {
            LOG.error(String.format("Файл %s не найден!", propFile.toString()));
        } catch (IOException e) {
            LOG.error(String.format("Ошибка чтения файла %s!", propFile.toString()));
        }

        if (properties.getProperty(UPDATE_PERIOD) != null &&
                !properties.getProperty(UPDATE_PERIOD).isEmpty()) {
            this.updatePeriod = Integer.parseInt(properties.getProperty(UPDATE_PERIOD));
        } else {
            this.updatePeriod = 10;
        }
        if (properties.getProperty(AUTO_UPDATE) != null &&
                !properties.getProperty(AUTO_UPDATE).isEmpty()) {
            this.autoUpdate = Boolean.parseBoolean(properties.getProperty(AUTO_UPDATE));
        } else {
            this.autoUpdate = true;
        }
        if (properties.getProperty(ONLY_BLOCKED) != null &&
                !properties.getProperty(ONLY_BLOCKED).isEmpty()) {
            this.onlyBlocked = Boolean.parseBoolean(properties.getProperty(ONLY_BLOCKED));
        } else {
            this.onlyBlocked = false;
        }
        if (properties.getProperty(UPDATE_UI_PERIOD) != null &&
                !properties.getProperty(UPDATE_UI_PERIOD).isEmpty()) {
            this.updateUIPeriod = Integer.parseInt(properties.getProperty(UPDATE_UI_PERIOD));
        } else {
            this.updateUIPeriod = 1;
        }
        if (properties.getProperty(LOGIN_TIMEOUT) != null &&
                !properties.getProperty(LOGIN_TIMEOUT).isEmpty()) {
            this.loginTimeout = Integer.parseInt(properties.getProperty(LOGIN_TIMEOUT));
        } else {
            this.loginTimeout = 10;
        }
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
     * Устанавливаем период обновления UI
     * @param updateUIPeriod
     */
    public void setUpdateUIPeriod(int updateUIPeriod) {
        this.updateUIPeriod = updateUIPeriod;
        saveProperties(UPDATE_UI_PERIOD, Integer.toString(updateUIPeriod));
    }

    /**
     * Получаем период обновления UI
     * @return the updateUIPeriod
     */
    public int getUpdateUIPeriod() {
        return updateUIPeriod;
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
