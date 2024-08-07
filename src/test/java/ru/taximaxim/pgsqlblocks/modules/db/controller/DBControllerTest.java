/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.modules.db.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBProcessStatus;
import ru.taximaxim.pgsqlblocks.modules.db.model.DBStatus;
import ru.taximaxim.pgsqlblocks.utils.Settings;

public class DBControllerTest {
    static class Listener implements DBControllerListener {
        CompletableFuture<DBController> lock = new CompletableFuture<>();

        @Override
        public void dbControllerStatusChanged(DBController controller, DBStatus newStatus) {}

        @Override
        public void dbControllerDidConnect(DBController controller) {}

        @Override
        public void dbControllerWillConnect(DBController controller) {}

        @Override
        public void dbControllerConnectionFailed(DBController controller, SQLException exception) {}

        @Override
        public void dbControllerDisconnectFailed(DBController controller, boolean forcedByUser, SQLException exception) {}

        @Override
        public void dbControllerDidDisconnect(DBController controller, boolean forcedByUser) {}

        @Override
        public void dbControllerWillUpdateProcesses(DBController controller) {}

        @Override
        public void dbControllerProcessesUpdated(DBController controller) {
            lock.complete(controller);
            lock = new CompletableFuture<>();
        }

        @Override
        public void dbControllerBlockedChanged(DBController controller) {}

        @Override
        public void dbControllerBlocksJournalChanged(DBController controller) {}
    }

    private static final Config CONFIG = ConfigFactory.load();
    private static final long DELAY_MS = CONFIG.getLong("pgsqlblocks-test-configs.delay-ms");
    private static final String REMOTE_HOST = CONFIG.getString("pgsqlblocks-test-configs.remote-host");
    private static final String REMOTE_DB = CONFIG.getString("pgsqlblocks-test-configs.remote-db");
    private static final String REMOTE_USERNAME = CONFIG.getString("pgsqlblocks-test-configs.remote-username");
    private static final String REMOTE_PASSWORD = CONFIG.getString("pgsqlblocks-test-configs.remote-password");
    private static final String CREATE_RULE_SQL = CONFIG.getString("pgsqlblocks-test-configs.create-rule-sql");
    private static final String TEST_DROP_RULE_SQL = CONFIG.getString("pgsqlblocks-test-configs.drop-rule-sql");
    private static final String TEST_SELECT_1000_SQL = CONFIG.getString("pgsqlblocks-test-configs.select-1000-sql");
    private static final String TEST_SELECT_SLEEP_SQL = CONFIG.getString("pgsqlblocks-test-configs.select-sleep-sql");
    private static final String TEST_CREATE_INDEX_SQL = CONFIG.getString("pgsqlblocks-test-configs.create-index-sql");

    private static DBController testDbc;
    private final List<ConnInfo> connectionList = new ArrayList<>();
    private final List<Thread> threadList = new ArrayList<>();
    private static final Listener LISTENER = new Listener();
    private static GenericContainer<?> postgres;
    private static Connection conn;

    @BeforeClass
    public static void initialize() {
        postgres = new PostgreSQLContainer("postgres:15.2-alpine")
                .withUsername(REMOTE_USERNAME)
                .withPassword(REMOTE_PASSWORD)
                .withDatabaseName(REMOTE_DB)
                .withExposedPorts(5432);
        postgres.start();
        DBModel model = new DBModel("TestDbc", REMOTE_HOST, postgres.getFirstMappedPort().toString(),
                REMOTE_DB, "", REMOTE_USERNAME, REMOTE_PASSWORD, true, true);
        testDbc = new DBController(Settings.getInstance(), model, null);
        testDbc.connectAsync();
        testDbc.addListener(LISTENER);
    }

    @AfterClass
    public static void terminate() throws SQLException {
        testDbc.disconnect(true);
        conn.close();
        postgres.close();

    }

    @Before
    public void beforeTest() throws IOException, SQLException {
        Integer port = postgres.getFirstMappedPort();
        Connection conn1 = getNewConnector(port);
        connectionList.add(new ConnInfo(getPid(conn1), conn1));

        Connection conn2 = getNewConnector(port);
        connectionList.add(new ConnInfo(getPid(conn2), conn2));

        Connection conn3 = getNewConnector(port);
        connectionList.add(new ConnInfo(getPid(conn3), conn3));

        /* prepare db */
        String query = loadQuery(CONFIG.getString("pgsqlblocks-test-configs.testing-dump-sql"));
        try (PreparedStatement prSt = getSingleConnection().prepareStatement(query)) {
            prSt.execute();
        }
        /* create rule */
        query = loadQuery(CREATE_RULE_SQL);
        try (PreparedStatement prSt = getSingleConnection().prepareStatement(query)) {
            prSt.execute();
        }
    }

    @After
    public void afterTest() throws SQLException {
        try (PreparedStatement termPs = getSingleConnection().prepareStatement(CONFIG.getString("pgsqlblocks-test-configs.pg-terminate-backend"))) {
            for (ConnInfo connInfo : connectionList) {
                termPs.setInt(1, connInfo.getPid());
                ResultSet result = termPs.executeQuery();
                if (result.next()) {
                    boolean terminatedSuccesed = result.getBoolean(CONFIG.getString("pgsqlblocks-test-configs.terminated-succesed"));
                    connInfo.getConnection().close();
                    assertTrue("Could not terminate process pid " + connInfo.getPid(), terminatedSuccesed);
                }
            }
        }
        connectionList.clear();

        for (Thread thread : threadList) {
            thread.interrupt();
        }
        threadList.clear();
    }

    @Test
    public void testMultipleLocks() throws IOException, SQLException, InterruptedException, ExecutionException {
        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));
        PreparedStatement statement3 = connectionList.get(2).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));

        runThreads(statement2, statement3, statement1);

        // wait until processes are updated
        CompletableFuture<DBController> lock = LISTENER.lock;
        testDbc.updateProcesses();
        lock.get();

        List<DBProcess> processes = testDbc.getProcesses();

        Stream<DBProcess> allGrandChildren = processes.stream().flatMap(l -> l.getChildren().stream());

        Optional<DBProcess> proc1 = allGrandChildren.filter(x -> x.getPid() == connectionList.get(0).getPid()).findFirst();
        Optional<DBProcess> proc2 = processes.stream().filter(x -> x.getPid() == connectionList.get(1).getPid()).findFirst();
        Optional<DBProcess> proc3 = processes.stream().filter(x -> x.getPid() == connectionList.get(2).getPid()).findFirst();

        assertTrue(proc1.isPresent());
        assertTrue(proc2.isPresent());
        assertTrue(proc3.isPresent());

        assertEquals(DBProcessStatus.BLOCKED, proc1.get().getStatus());
        assertEquals(DBProcessStatus.BLOCKING, proc2.get().getStatus());
        assertEquals(DBProcessStatus.BLOCKING, proc3.get().getStatus());

        assertTrue(proc2.get().hasChildren());
        assertTrue(proc1.get().getParents().stream().anyMatch(x -> x.getPid() == proc2.get().getPid()));
        assertTrue(proc3.get().hasChildren());
        assertTrue(proc1.get().getParents().stream().anyMatch(x -> x.getPid() == proc3.get().getPid()));
    }

    @Test
    public void testReproWaitingLocks() throws IOException, SQLException, InterruptedException, ExecutionException {
        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(loadQuery(TEST_SELECT_SLEEP_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(loadQuery(TEST_CREATE_INDEX_SQL));

        runThreads(statement1, statement2);

        // wait until processes are updated
        CompletableFuture<DBController> lock = LISTENER.lock;
        testDbc.updateProcesses();
        lock.get();

        List<DBProcess> processes = testDbc.getProcesses();
        Stream<DBProcess> allGrandChildren = processes.stream().flatMap(l -> l.getChildren().stream());

        Optional <DBProcess> proc1 = processes.stream().filter(x -> x.getPid() == connectionList.get(0).getPid()).findFirst();
        Optional <DBProcess> proc2 = allGrandChildren.filter(x -> x.getPid() == connectionList.get(1).getPid()).findFirst();

        assertTrue(proc1.isPresent());
        assertTrue(proc2.isPresent());

        assertEquals(DBProcessStatus.BLOCKING, proc1.get().getStatus());
        assertEquals(DBProcessStatus.BLOCKED, proc2.get().getStatus());

        assertTrue(proc1.get().hasChildren());
        assertTrue(proc2.get().getParents().stream().anyMatch(x -> x.getPid() == proc1.get().getPid()));
    }

    @Test
    public void testOrderedLocks() throws IOException, SQLException, InterruptedException, ExecutionException {
        /* create rule */
        try (PreparedStatement prSt = getSingleConnection().prepareStatement(loadQuery(CREATE_RULE_SQL))) {
            prSt.execute();
        }

        assertTrue(connectionList.get(0).getPid() < connectionList.get(1).getPid());

        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));

        runThreads(statement2, statement1);

        // wait until processes are updated
        CompletableFuture<DBController> lock = LISTENER.lock;
        testDbc.updateProcesses();
        lock.get();

        List<DBProcess> processes = testDbc.getProcesses();

        Stream<DBProcess> allGrandChildren = processes.stream().flatMap(l -> l.getChildren().stream());

        Optional <DBProcess> proc1 = allGrandChildren.filter(x -> x.getPid() == connectionList.get(0).getPid()).findFirst();
        Optional <DBProcess> proc2 = processes.stream().filter(x -> x.getPid() == connectionList.get(1).getPid()).findFirst();

        assertTrue(proc1.isPresent());
        assertTrue(proc2.isPresent());

        assertEquals(DBProcessStatus.BLOCKING, proc2.get().getStatus());
        assertEquals(DBProcessStatus.BLOCKED, proc1.get().getStatus());

        assertTrue(proc2.get().hasChildren());
        assertTrue(proc1.get().getParents().stream().anyMatch(x -> x.getPid() == proc2.get().getPid()));
    }

    @Test
    public void testTripleLocks() throws IOException, SQLException, InterruptedException, ExecutionException {
        /* create rule */
        try (PreparedStatement prSt = getSingleConnection().prepareStatement(loadQuery(CREATE_RULE_SQL))) {
            prSt.execute();
        }

        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement3 = connectionList.get(2).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));

        runThreads(statement1, statement2, statement3);

        // wait until processes are updated
        CompletableFuture<DBController> lock = LISTENER.lock;
        testDbc.updateProcesses();
        lock.get();

        List<DBProcess> processes = testDbc.getProcesses();

        List<DBProcess> allGrandChildren = processes.stream().flatMap(l -> l.getChildren().stream()).collect(Collectors.toList());

        Optional <DBProcess> proc1 = processes.stream().filter(x -> x.getPid() == connectionList.get(0).getPid()).findFirst();
        Optional <DBProcess> proc2 = allGrandChildren.stream().filter(x -> x.getPid() == connectionList.get(1).getPid()).findFirst();
        Optional <DBProcess> proc3 = allGrandChildren.stream().filter(x -> x.getPid() == connectionList.get(2).getPid()).findFirst();


        assertTrue(proc1.isPresent());
        assertTrue(proc2.isPresent());
        assertTrue(proc3.isPresent());

        assertEquals(DBProcessStatus.BLOCKING, proc1.get().getStatus());
        assertEquals(DBProcessStatus.BLOCKING, proc2.get().getStatus());
        assertEquals(DBProcessStatus.BLOCKED, proc3.get().getStatus());

        assertTrue(proc1.get().hasChildren());
        assertTrue(proc2.get().getParents().stream().anyMatch(x -> x.getPid() == proc1.get().getPid()));
        assertTrue(proc3.get().getParents().stream().anyMatch(x -> x.getPid() == proc1.get().getPid()));

        assertTrue(proc2.get().hasChildren());
        assertTrue(proc3.get().getParents().stream().anyMatch(x -> x.getPid() == proc2.get().getPid()));
    }

    private Connection getSingleConnection() throws SQLException {
        if (conn == null) {
            conn = getNewConnector(postgres.getFirstMappedPort());
        }
        return conn;
    }

    private Connection getNewConnector(Integer port) throws SQLException {
        DriverManager.setLoginTimeout(10);
        return DriverManager.getConnection(
                String.format("jdbc:postgresql://%1$s:%2$s/%3$s", REMOTE_HOST, port, REMOTE_DB),
                REMOTE_USERNAME,
                REMOTE_PASSWORD);
    }

    private void runThreads(PreparedStatement... statements) throws InterruptedException {
        for (PreparedStatement statement : statements) {
            Thread thread = getNewThread(statement);
            threadList.add(thread);
            thread.start();
            Thread.sleep(DELAY_MS);
        }
    }

    private Thread getNewThread(PreparedStatement statement) {
        return new Thread(() -> {
            try {
                statement.execute();
            } catch (SQLException e) {
                // no-op
            }
        }
        );
    }

    private static int getPid(Connection connection) throws SQLException {
        int pid = 0;
        ResultSet result = connection.prepareStatement(CONFIG.getString("pgsqlblocks-test-configs.select-pg-backend-pid")).executeQuery();
        if (result.next()) {
            pid = result.getInt(CONFIG.getString("pgsqlblocks-test-configs.pg-backend"));
        }
        return pid;
    }

    private String loadQuery(String filename) throws IOException {
        try {
            Path path = Paths.get(ClassLoader.getSystemResource(filename).toURI());
            List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
            return lines.stream().collect(Collectors.joining(System.lineSeparator()));
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}

class ConnInfo {
    private final int pid;
    private final Connection connection;

    public ConnInfo(int pid, Connection connection) {
        this.pid = pid;
        this.connection = connection;
    }

    public int getPid() {
        return pid;
    }

    public Connection getConnection() {
        return connection;
    }
}
