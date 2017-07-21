package ru.taximaxim.pgsqlblocks;


import ru.taximaxim.pgsqlblocks.modules.application.controller.ApplicationController;

public class PgSqlBlocks {

    private static PgSqlBlocks instance;

    private ApplicationController applicationController;

    private PgSqlBlocks() {
        applicationController = new ApplicationController();
    }

    public static void main(String[] args) {
        PgSqlBlocks.getInstance().launchWithArgs(args);
    }

    public static PgSqlBlocks getInstance() {
        if (instance == null)
            instance = new PgSqlBlocks();
        return instance;
    }

    public void launchWithArgs(String[] args) {
        applicationController.launchWithArgs(args);
    }

}