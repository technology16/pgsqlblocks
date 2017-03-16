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
package ru.taximaxim.pgsqlblocks.dbcdata;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class DbcDataRunner implements Runnable {
    private static final Logger LOG = Logger.getLogger(DbcDataRunner.class);
    private Settings settings = Settings.getInstance();
    private ResourceBundle resourceBundle = settings.getResourceBundle();
    private DbcData dbcData;

    public DbcDataRunner(DbcData data) {
        dbcData = data;
    }

    @Override
    public void run() {
        try {
            dbcData.setInUpdateState(true);
            if (!dbcData.isConnected()) {
                LOG.debug(MessageFormat.format(resourceBundle.getString("db_connecting"), dbcData.getName()));
                dbcData.connect();
            }
            if (dbcData.getStatus() == DbcStatus.CONNECTION_ERROR) {
                LOG.warn(MessageFormat.format(resourceBundle.getString("db_update_error"), dbcData.getName()));
                dbcData.stopUpdater();
            } else {
                LOG.info(MessageFormat.format(resourceBundle.getString("db_updating"), dbcData.getName()));
                dbcData.setProcess(dbcData.getProcessTree(true));
            }

            LOG.debug(MessageFormat.format(resourceBundle.getString("db_finish_updating"), dbcData.getName()));
        } catch (Exception e) {
            LOG.error(MessageFormat.format(resourceBundle.getString("db_error_on_connect"), e.getMessage()));
        } finally {
            dbcData.setInUpdateState(false);
            dbcData.notifyUpdated();
        }
    }
}
