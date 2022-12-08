/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2022 "Technology" LLC
 * %
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс для работы с настройка пользователя
 */
public final class Settings {

    private final List<SettingsListener> listeners = new ArrayList<>();

    public static final String[] SUPPORTED_LANGUAGES = { "ru", "en" };
    private static final Logger LOG = LogManager.getLogger(Settings.class);

    private static final String UPDATE_PERIOD = "update_period";
    private static final String LIMIT_BLOCKS = "limit_blocks";

    private static final String LOGIN_TIMEOUT = "login_timeout";
    private static final String AUTO_UPDATE = "auto_update";
    private static final String ONLY_BLOCKED = "only_blocked";
    private static final String SHOW_IDLE = "show_idle";
    private static final String SHOW_TOOL_TIP = "show_tool_tip";
    private static final String SHOW_BACKEND_PID = "show_backend_pid";
    private static final String CONFIRM_REQUIRED = "confirm_required";
    private static final String CONFIRM_EXIT = "confirm_exit";
    private static final String SHOW_LOG_MESSAGES = "show_log_messages";
    private static final String CURRENT_LOCALE = "current_locale";

    private int updatePeriodSeconds;
    private int limitBlocks;
    private final int loginTimeout;

    private boolean autoUpdate;

    private boolean showIdle;
    private boolean showToolTip;
    private boolean confirmRequired;
    private boolean showBackendPid;
    private boolean confirmExit;

    private final Properties properties;
    private final File propFile;
    private final Locale locale;
    private final ResourceBundle resources;

    private static Settings instance;

    private Settings() {
        Properties defaults = new Properties();
        defaults.put(UPDATE_PERIOD, "10");
        defaults.put(LIMIT_BLOCKS, "10000");
        defaults.put(AUTO_UPDATE, "true");
        defaults.put(ONLY_BLOCKED, "false");
        defaults.put(LOGIN_TIMEOUT, "10");
        defaults.put(SHOW_IDLE, "true");
        defaults.put(SHOW_TOOL_TIP, "false");
        defaults.put(SHOW_BACKEND_PID, "true");
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

        this.updatePeriodSeconds = Integer.parseInt(properties.getProperty(UPDATE_PERIOD));
        this.limitBlocks = Integer.parseInt(properties.getProperty(LIMIT_BLOCKS));
        this.autoUpdate = Boolean.parseBoolean(properties.getProperty(AUTO_UPDATE));
        this.loginTimeout = Integer.parseInt(properties.getProperty(LOGIN_TIMEOUT));
        this.showIdle = Boolean.parseBoolean(properties.getProperty(SHOW_IDLE));
        this.showToolTip = Boolean.parseBoolean(properties.getProperty(SHOW_TOOL_TIP));
        this.showBackendPid = Boolean.parseBoolean(properties.getProperty(SHOW_BACKEND_PID));
        this.confirmRequired = Boolean.parseBoolean(properties.getProperty(CONFIRM_REQUIRED));
        this.confirmExit = Boolean.parseBoolean(properties.getProperty(CONFIRM_EXIT));
        this.locale = new Locale.Builder().setLanguageTag(properties.getProperty(CURRENT_LOCALE)).build();

        resources = ResourceBundle.getBundle(ru.taximaxim.pgsqlblocks.l10n.PgSqlBlocks.class.getName(), locale);
    }

    public static Settings getInstance() {
        if(instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public void addListener(SettingsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SettingsListener listener) {
        listeners.remove(listener);
    }

    public void setLanguage(String language) {
        saveProperties(CURRENT_LOCALE, language);
    }

    /**
     * Устанавливаем период обновления
     */
    public void setUpdatePeriodSeconds(int updatePeriodSeconds) {
        if (this.updatePeriodSeconds != updatePeriodSeconds) {
            this.updatePeriodSeconds = updatePeriodSeconds;
            saveProperties(UPDATE_PERIOD, Integer.toString(updatePeriodSeconds));
            listeners.forEach(listener -> listener.settingsUpdatePeriodChanged(this.updatePeriodSeconds));
        }
    }

    /**
     * Получаем период обновления
     * @return the updatePeriodSeconds
     */
    public int getUpdatePeriodSeconds() {
        return updatePeriodSeconds;
    }

    /**
     * Устанавливаем количество заблоченных процессов
     */
    public void setLimitBlocks(int limitBlocks) {
        if (this.limitBlocks != limitBlocks) {
            this.limitBlocks = limitBlocks;
            saveProperties(LIMIT_BLOCKS, Integer.toString(limitBlocks));
            listeners.forEach(listener -> listener.settingsLimitBlocksChanged(limitBlocks));
        }
    }

    /**
     * Получаем количество заблокированных процессов
     * @return the limitBlocks
     */
    public int getLimitBlocks() {
        return limitBlocks;
    }
    /**
     * Gets the maximum time in seconds that a driver can wait
     * when attempting to log in to a database.
     *
     * @return the driver login time limit in seconds
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
        if (this.autoUpdate != autoUpdate) {
            this.autoUpdate = autoUpdate;
            saveProperties(AUTO_UPDATE, Boolean.toString(autoUpdate));
            listeners.forEach(listener -> listener.settingsAutoUpdateChanged(this.autoUpdate));
        }
    }

    /**
     * Устанавливаем флаг отображения бездействующих процессов
     * @param showIdle the showIdle to set
     */
    public void setShowIdle(boolean showIdle) {
        if (this.showIdle != showIdle) {
            this.showIdle = showIdle;
            saveProperties(SHOW_IDLE, Boolean.toString(showIdle));
            listeners.forEach(listener -> listener.settingsShowIdleChanged(this.showIdle));
        }
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
        if (this.showBackendPid != showBackendPid) {
            this.showBackendPid = showBackendPid;
            saveProperties(SHOW_BACKEND_PID, Boolean.toString(showBackendPid));
            listeners.forEach(listener -> listener.settingsShowBackendPidChanged(this.showBackendPid));
        }
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
        saveProperties(CONFIRM_REQUIRED, String.valueOf(confirmRequired));
    }

    public boolean isConfirmExit() {
        return confirmExit;
    }

    public void setConfirmExit(boolean confirmExit) {
        this.confirmExit = confirmExit;
        saveProperties(CONFIRM_EXIT, String.valueOf(confirmExit));
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
