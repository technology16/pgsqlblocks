/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
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

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.SortColumn;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс для работы с настройка пользователя
 */
public final class Settings {
    public static final String[] SUPPORTED_LANGUAGES = {"ru", "en"};
    private static final Logger LOG = Logger.getLogger(Settings.class);

    private static final String UPDATE_PERIOD = "update_period";
    private static final String LOGIN_TIMEOUT = "login_timeout";
    private static final String AUTO_UPDATE = "auto_update";
    private static final String ONLY_BLOCKED = "only_blocked";
    private static final String SHOW_IDLE = "show_idle";
    private static final String SHOW_TOOL_TIP = "show_tool_tip";
    private static final String SHOW_BACKEND_PID = "show_backend_pid";
    private static final String COLUMNS_LIST = "columns_list";
    private static final String CONFIRM_REQUIRED = "confirm_required";
    private static final String CONFIRM_EXIT = "confirm_exit";
    private static final String SHOW_LOG_MESSAGES = "show_log_messages";
    private static final String CURRENT_LOCALE = "current_locale";

    private int updatePeriod;
    private int loginTimeout;

    private boolean autoUpdate;

    private boolean onlyBlocked;
    private boolean showIdle;
    private boolean showToolTip;
    private boolean confirmRequired;
    private boolean showBackendPid;
    private boolean showLogMessages;
    private boolean confirmExit;

    private Set<SortColumn> columnsList;

    private Properties properties;
    private File propFile;
    private final Locale locale;
    private final ResourceBundle resources;

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
        defaults.put(COLUMNS_LIST, Arrays.stream(SortColumn.values()).map(Enum::name).collect(Collectors.joining(",")));
        defaults.put(CONFIRM_REQUIRED, "true");
        defaults.put(CONFIRM_EXIT, "true");
        defaults.put(SHOW_LOG_MESSAGES, "true");
        defaults.put(CURRENT_LOCALE, new Locale.Builder().setLanguage(SUPPORTED_LANGUAGES[0]).build().toLanguageTag());

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
        this.columnsList = Arrays.stream(properties.getProperty(COLUMNS_LIST).split(","))
                                            .map(SortColumn::valueOf).collect(Collectors.toSet());
        this.confirmRequired = Boolean.parseBoolean(properties.getProperty(CONFIRM_REQUIRED));
        this.confirmExit = Boolean.parseBoolean(properties.getProperty(CONFIRM_EXIT));
        this.showLogMessages = Boolean.parseBoolean(properties.getProperty(SHOW_LOG_MESSAGES));
        this.locale = new Locale.Builder().setLanguageTag(properties.getProperty(CURRENT_LOCALE)).build();

        resources = ResourceBundle.getBundle(ru.taximaxim.pgsqlblocks.l10n.PgSqlBlocks.class.getName(), locale);
    }

    public static Settings getInstance() {
        if(instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public void setLanguage(String language) {
        saveProperties(CURRENT_LOCALE, language);
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

    public Set<SortColumn> getColumnsList() {
        return columnsList;
    }

    public void setColumnsList(Set<SortColumn> columnsList) {
        this.columnsList = columnsList;
        saveProperties(COLUMNS_LIST, columnsList.stream().map(Enum::name).collect(Collectors.joining(",")));
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
        saveProperties(CONFIRM_REQUIRED, String.valueOf(confirmRequired));
    }

    public boolean isConfirmExit() {
        return confirmExit;
    }

    public void setConfirmExit(boolean confirmExit) {
        this.confirmExit = confirmExit;
        saveProperties(CONFIRM_EXIT, String.valueOf(confirmExit));
    }

    /**
     * Устанавливаем флаг отображения логов
     * @param showLogMessages флаг отображения логов
     * @see #setShowLogMessages(boolean)
     */
    public void setShowLogMessages(boolean showLogMessages) {
        this.showLogMessages = showLogMessages;
        saveProperties(SHOW_LOG_MESSAGES, Boolean.toString(showLogMessages));
    }

    /**
     * Устанавливаем флаг отображения логов
     * @return the showLogMessages
     * @see #getShowLogMessages()
     */
    public boolean getShowLogMessages() {
        return showLogMessages;
    }

    public Locale getLocale() {
        return locale;
    }

    public ResourceBundle getResourceBundle() {
        return resources;
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
