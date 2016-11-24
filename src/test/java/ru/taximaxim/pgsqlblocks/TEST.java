package ru.taximaxim.pgsqlblocks;

public interface TEST {
    String REMOTE_HOST = "10.84.0.6";
    String REMOTE_DB = "maindb_dev2";
//    String REMOTE_DB = "postgres";
    String REMOTE_PORT = "5432";
    String REMOTE_USERNAME = "pgsqlblocks_test";
    String REMOTE_PASSWORD = "12345678";

//    String REMOTE_DB_PATTERN = "pgsqlblocks_testing_{0}";

    String SELECT_PG_BACKEND_PID = "select pg_backend_pid();";
    String PID = "pg_backend_pid";

    String TESTING_DUMP_SQL = "test/testing_dump.sql";
    String CREATE_RULE_SQL = "test/create_rule.sql";

    String TEST_COMMON_LOCKS_1_SQL = "test/common_locks_1.sql";
    String TEST_COMMON_LOCKS_2_SQL = "test/common_locks_2.sql";
    String[] PG_TERMINATE_BACKEND = {"select pg_terminate_backend(",");"};
    String TERMINATED_SUCCESED = "pg_terminate_backend";
}