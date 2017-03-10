/*
 * Copyright 2017 "Technology" LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.taximaxim.pgsqlblocks.dbcdata;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

public class DbcDataRunner implements Runnable {
    private static final Logger LOG = Logger.getLogger(DbcDataRunner.class);
    private DbcData dbcData;

    public DbcDataRunner(DbcData data) {
        dbcData = data;
    }

    @Override
    public void run() {
        try {
            dbcData.setInUpdateState(true);
            if (!dbcData.isConnected()) {
                LOG.debug(MessageFormat.format("  Connecting \"{0}\"...", dbcData.getName()));
                dbcData.connect();
            }
            if (dbcData.getStatus() == DbcStatus.CONNECTION_ERROR) {
                LOG.warn(MessageFormat.format("  Error on DbcData: {0}", dbcData.getName()));
                dbcData.stopUpdater();
            } else {
                LOG.info(MessageFormat.format("  Updating \"{0}\"...", dbcData.getName()));
                dbcData.setProcess(dbcData.getProcessTree(true));
            }

            LOG.debug(MessageFormat.format("  Finish updating \"{0}\"...", dbcData.getName()));
        } catch (Exception e) {
            LOG.error(MessageFormat.format("  Error on connect or update DbcData: {0}", e.getMessage()));
        } finally {
            dbcData.setInUpdateState(false);
            dbcData.notifyUpdated();
        }
    }
}
