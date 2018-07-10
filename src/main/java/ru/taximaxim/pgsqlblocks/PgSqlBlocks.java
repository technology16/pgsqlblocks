/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
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
package ru.taximaxim.pgsqlblocks;

import ru.taximaxim.pgsqlblocks.modules.application.controller.ApplicationController;

public class PgSqlBlocks {
    public static final String APP_NAME = "pgSqlBlocks " + PgSqlBlocks.class.getPackage().getImplementationVersion();
    private static PgSqlBlocks instance;

    private final ApplicationController applicationController;

    private PgSqlBlocks() {
        applicationController = new ApplicationController();
    }

    public static void main(String[] args) {
        PgSqlBlocks.getInstance().launch();
    }

    public static PgSqlBlocks getInstance() {
        if (instance == null)
            instance = new PgSqlBlocks();
        return instance;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    private void launch() {
        applicationController.launch();
    }
}
