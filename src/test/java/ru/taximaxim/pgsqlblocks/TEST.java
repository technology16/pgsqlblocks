package ru.taximaxim.pgsqlblocks;

public interface TEST {
    String REMOTE_HOST = "10.84.0.6";
    String REMOTE_DB = "maindb_dev2";
    String REMOTE_PORT = "5432";
    String REMOTE_USERNAME = "pgsqlblocks_test";
    String REMOTE_PASSWORD = "12345678";

    String SELECT_PG_BACKEND_PID = "select pg_backend_pid();";
    String[] PG_TERMINATE_BACKEND = {"select pg_terminate_backend(",");"};
    String TERMINATED_SUCCESED = "pg_terminate_backend";
    String PID = "pg_backend_pid";

    String TESTING_DUMP_SQL = "test/testing_dump.sql";
    String CREATE_RULE_SQL = "test/create_rule.sql";
    String TEST_DROP_RULE_SQL = "test/drop_rule.sql";
    String TEST_SELECT_1000_SQL = "test/select_1000.sql";
    String TEST_SELECT_SLEEP_SQL = "test/select_sleep.sql";
    String TEST_CREATE_INDEX_SQL = "test/create_index.sql";
}