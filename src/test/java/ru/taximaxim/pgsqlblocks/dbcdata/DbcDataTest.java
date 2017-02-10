package ru.taximaxim.pgsqlblocks.dbcdata;

import org.junit.*;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.taximaxim.pgsqlblocks.TEST.*;

public class DbcDataTest {
    private static final long DELAY_MS = 250;
    private static DbcData testDbc;
    private static List<ConnInfo> connectionList = new ArrayList<>();
    private static List<Thread> threadList = new ArrayList<>();

    @BeforeClass
    public static void initialize() throws IOException {
        testDbc = new DbcData("TestDbc", REMOTE_HOST,  REMOTE_PORT, REMOTE_DB,
                REMOTE_USERNAME,  REMOTE_PASSWORD,  true);
        testDbc.connect();
    }

    @AfterClass
    public static void terminate() throws SQLException {
        testDbc.disconnect();
    }

    @Before
    public void beforeTest() throws IOException, SQLException {
        Connection conn1 = getNewConnector();
        connectionList.add(new ConnInfo(getPid(conn1), conn1));

        Connection conn2 = getNewConnector();
        connectionList.add(new ConnInfo(getPid(conn2), conn2));

        Connection conn3 = getNewConnector();
        connectionList.add(new ConnInfo(getPid(conn3), conn3));

        /* prepare db */
        testDbc.getConnection().prepareStatement(loadQuery(TESTING_DUMP_SQL)).execute();
        /* create rule */
        testDbc.getConnection().prepareStatement(loadQuery(CREATE_RULE_SQL)).execute();
    }

    @After
    public void afterTest() throws SQLException {
        try (PreparedStatement termPs = testDbc.getConnection().prepareStatement(PG_TERMINATE_BACKEND)) {
            for (ConnInfo connInfo : connectionList) {
                termPs.setInt(1, connInfo.getPid());
                ResultSet result = termPs.executeQuery();
                if (result.next()) {
                    boolean terminatedSuccesed = result.getBoolean(TERMINATED_SUCCESED);
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
    public void testMultipleLocks() throws IOException, SQLException, InterruptedException {
        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));
        PreparedStatement statement3 = connectionList.get(2).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));

        runThreads(statement2, statement3, statement1);

        Process rootProcess = testDbc.getProcessTree(true);

        List<Process> allGrandChild = rootProcess.getChildren().stream().
                flatMap(l -> l.getChildren().stream()).
                collect(Collectors.toList());

        Optional <Process> proc1 = allGrandChild.stream().filter(x -> x.getPid() == connectionList.get(0).getPid()).findFirst();
        Optional <Process> proc2 = rootProcess.getChildren().stream().filter(x -> x.getPid() == connectionList.get(1).getPid()).findFirst();
        Optional <Process> proc3 = rootProcess.getChildren().stream().filter(x -> x.getPid() == connectionList.get(2).getPid()).findFirst();

        assertTrue(proc1.isPresent());
        assertTrue(proc2.isPresent());
        assertTrue(proc3.isPresent());

        assertEquals(ProcessStatus.BLOCKED, proc1.get().getStatus());
        assertEquals(ProcessStatus.BLOCKING, proc2.get().getStatus());
        assertEquals(ProcessStatus.BLOCKING, proc3.get().getStatus());

        assertTrue(proc2.get().hasChildren());
        assertTrue(proc1.get().getParents().stream().anyMatch(x -> x.getPid() == proc2.get().getPid()));
        assertTrue(proc3.get().hasChildren());
        assertTrue(proc1.get().getParents().stream().anyMatch(x -> x.getPid() == proc3.get().getPid()));
    }

    @Test
    public void testReproWaitingLocks() throws IOException, SQLException, InterruptedException {
        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(loadQuery(TEST_SELECT_SLEEP_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(loadQuery(TEST_CREATE_INDEX_SQL));

        runThreads(statement1, statement2);

        Process rootProcess = testDbc.getProcessTree(true);
        List<Process> allGrandChild = rootProcess.getChildren().stream().
                flatMap(l -> l.getChildren().stream()).
                collect(Collectors.toList());

        Optional <Process> proc1 = rootProcess.getChildren().stream().filter(x -> x.getPid() == connectionList.get(0).getPid()).findFirst();
        Optional <Process> proc2 = allGrandChild.stream().filter(x -> x.getPid() == connectionList.get(1).getPid()).findFirst();

        assertTrue(proc1.isPresent());
        assertTrue(proc2.isPresent());

        assertEquals(ProcessStatus.BLOCKING, proc1.get().getStatus());
        assertEquals(ProcessStatus.BLOCKED, proc2.get().getStatus());

        assertTrue(proc1.get().hasChildren());
        assertTrue(proc2.get().getParents().stream().anyMatch(x -> x.getPid() == proc1.get().getPid()));
    }

    @Test
    public void testOrderedLocks() throws IOException, SQLException, InterruptedException {
        /* create rule */
        testDbc.getConnection().prepareStatement(loadQuery(CREATE_RULE_SQL)).execute();

        assertTrue(connectionList.get(0).getPid() < connectionList.get(1).getPid());

        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));

        runThreads(statement2, statement1);

        Process rootProcess = testDbc.getProcessTree(true);

        List<Process> allGrandChild = rootProcess.getChildren().stream().
                flatMap(l -> l.getChildren().stream()).
                collect(Collectors.toList());

        Optional <Process> proc1 = allGrandChild.stream().filter(x -> x.getPid() == connectionList.get(0).getPid()).findFirst();
        Optional <Process> proc2 = rootProcess.getChildren().stream().filter(x -> x.getPid() == connectionList.get(1).getPid()).findFirst();

        assertTrue(proc1.isPresent());
        assertTrue(proc2.isPresent());

        assertEquals(ProcessStatus.BLOCKING, proc2.get().getStatus());
        assertEquals(ProcessStatus.BLOCKED, proc1.get().getStatus());

        assertTrue(proc2.get().hasChildren());
        assertTrue(proc1.get().getParents().stream().anyMatch(x -> x.getPid() == proc2.get().getPid()));
    }

    // TODO: remove ignore and fix test after solving the issue #12290
    @Test
    @Ignore // renable when #12290 is fixed
    public void testTripleLocks() throws IOException, SQLException, InterruptedException {
        /* create rule */
        testDbc.getConnection().prepareStatement(loadQuery(CREATE_RULE_SQL)).execute();

        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement3 = connectionList.get(2).getConnection().prepareStatement(loadQuery(TEST_SELECT_1000_SQL));

        runThreads(statement1, statement2, statement3);

        Process rootProcess = testDbc.getProcessTree(true);

        List<Process> allGrandChild = rootProcess.getChildren().stream().
                flatMap(l -> l.getChildren().stream()).
                collect(Collectors.toList());

        Optional <Process> proc1 = rootProcess.getChildren().stream().filter(x -> x.getPid() == connectionList.get(0).getPid()).findFirst();
        Optional <Process> proc2 = allGrandChild.stream().filter(x -> x.getPid() == connectionList.get(1).getPid()).findFirst();
        Optional <Process> proc3 = allGrandChild.stream().filter(x -> x.getPid() == connectionList.get(2).getPid()).findFirst();


        assertTrue(proc1.isPresent());
        assertTrue(proc2.isPresent());
        assertTrue(proc3.isPresent());

        assertEquals(ProcessStatus.BLOCKING, proc1.get().getStatus());
        assertEquals(ProcessStatus.BLOCKED, proc2.get().getStatus());
        assertEquals(ProcessStatus.BLOCKING, proc3.get().getStatus());

        assertTrue(proc1.get().hasChildren());
        assertTrue(proc2.get().getParents().stream().anyMatch(x -> x.getPid() == proc1.get().getPid()));
        assertTrue(proc3.get().getParents().stream().anyMatch(x -> x.getPid() == proc1.get().getPid()));

        assertTrue(proc2.get().hasChildren());
        assertTrue(proc3.get().getParents().stream().anyMatch(x -> x.getPid() == proc2.get().getPid()));
    }

    private Connection getNewConnector() throws SQLException {
        DriverManager.setLoginTimeout(10);
        return DriverManager.getConnection(
                String.format("jdbc:postgresql://%1$s:%2$s/%3$s", REMOTE_HOST, REMOTE_PORT, REMOTE_DB),
                REMOTE_USERNAME,
                REMOTE_PASSWORD);
    }

    @SuppressWarnings("squid:S2925")
    private void runThreads(PreparedStatement... statements) throws SQLException, InterruptedException {
        for (PreparedStatement statement : statements) {
            Thread thread = getNewThread(statement);
            threadList.add(thread);
            thread.start();
            Thread.sleep(DELAY_MS);
        }
    }

    private Thread getNewThread(PreparedStatement statement) throws SQLException {
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
        ResultSet result = connection.prepareStatement(SELECT_PG_BACKEND_PID).executeQuery();
        if (result.next()) {
            pid = result.getInt(PID);
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
