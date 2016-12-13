package ru.taximaxim.pgsqlblocks.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.SortColumn;

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
    private static final String COLUMNS_LIST = "columns_list";

    private int updatePeriod;
    private int loginTimeout;

    private boolean autoUpdate;

    private boolean onlyBlocked;
    private boolean showIdle;

    private String columnsList;

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
        this.autoUpdate = !(properties.getProperty(AUTO_UPDATE) != null &&
                !properties.getProperty(AUTO_UPDATE).isEmpty()) || Boolean.parseBoolean(properties.getProperty(AUTO_UPDATE));
        this.onlyBlocked = properties.getProperty(ONLY_BLOCKED) != null
                && !properties.getProperty(ONLY_BLOCKED).isEmpty() && Boolean.parseBoolean(properties.getProperty(ONLY_BLOCKED));
        if (properties.getProperty(LOGIN_TIMEOUT) != null &&
                !properties.getProperty(LOGIN_TIMEOUT).isEmpty()) {
            this.loginTimeout = Integer.parseInt(properties.getProperty(LOGIN_TIMEOUT));
        } else {
            this.loginTimeout = 10;
        }

        this.showIdle = !(properties.getProperty(SHOW_IDLE) != null &&
                !properties.getProperty(SHOW_IDLE).isEmpty()) || Boolean.parseBoolean(properties.getProperty(SHOW_IDLE));

        if (properties.getProperty(COLUMNS_LIST) != null && !properties.getProperty(COLUMNS_LIST).isEmpty()) {
            this.columnsList = properties.getProperty(COLUMNS_LIST);
        } else {
            this.columnsList = StringUtils.join(SortColumn.values(), ",");
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

    public String getColumnsList() {
        return columnsList;
    }

    public void setColumnsList(String columnsList) {
        this.columnsList = columnsList;
        saveProperties(COLUMNS_LIST, columnsList);
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
