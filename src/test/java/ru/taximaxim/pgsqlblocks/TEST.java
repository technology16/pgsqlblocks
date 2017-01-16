package ru.taximaxim.pgsqlblocks;

public final class TEST {
    public static final String REMOTE_HOST = "10.84.0.6";
    public static final String REMOTE_DB = "maindb_dev2";
    public static final String REMOTE_PORT = "5432";
    public static final String REMOTE_USERNAME = "pgsqlblocks_test";
    public static final String REMOTE_PASSWORD = "12345678";

    public static final String SELECT_PG_BACKEND_PID = "select pg_backend_pid();";
    public static final String PG_TERMINATE_BACKEND = "select pg_terminate_backend(?);";
    public static final String TERMINATED_SUCCESED = "pg_terminate_backend";
    public static final String PID = "pg_backend_pid";

    public static final String TESTING_DUMP_SQL = "testing_dump.sql";
    public static final String CREATE_RULE_SQL = "create_rule.sql";
    public static final String TEST_DROP_RULE_SQL = "drop_rule.sql";
    public static final String TEST_SELECT_1000_SQL = "select_1000.sql";
    public static final String TEST_SELECT_SLEEP_SQL = "select_sleep.sql";
    public static final String TEST_CREATE_INDEX_SQL = "create_index.sql";
}