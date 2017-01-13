package ru.taximaxim.pgsqlblocks.dbcdata;

import org.apache.log4j.Logger;
import org.junit.*;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessStatus;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeBuilder;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.taximaxim.pgsqlblocks.TEST.*;

public class DbcDataTest {
    private static DbcData testDbc;
    private static final Logger LOG = Logger.getLogger(DbcDataTest.class);
    private static List<ConnInfo> connectionList = new ArrayList<>();

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
        testDbc.getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TESTING_DUMP_SQL)).execute();
        /* create rule */
        testDbc.getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(CREATE_RULE_SQL)).execute();
    }

    @After
    public void executionConns() throws SQLException {
        List<ConnInfo> newList = new ArrayList<>();
        for(ConnInfo connInfo : connectionList) {
            try (PreparedStatement termPs = testDbc.getConnection().prepareStatement(PG_TERMINATE_BACKEND)) {
                termPs.setInt(1, connInfo.getPid());
                LOG.info("Prepared query:" + termPs);
                ResultSet result = termPs.executeQuery();
                if (result.next()) {
                    boolean terminatedSuccesed = result.getBoolean(TERMINATED_SUCCESED);
                    LOG.info("Terminating the process pid:" + connInfo.getPid() + " is succeed:" + terminatedSuccesed);
                    
                    if (terminatedSuccesed) {
                        LOG.info("Process terminated:" + connInfo.getPid());
                        connInfo.getConnection().close();
                    } else {
                        LOG.info("Process cannot be terminated:" + connInfo.getPid());
                        newList.add(connInfo);
                    }
                }
                assert !result.isAfterLast();
            } catch (Exception e) {
                LOG.error("Error on prepared statement" + e.getMessage());
            }
        }
        connectionList = newList;
    }

    @Test
    public void testMultipleLocks() throws IOException, SQLException, InterruptedException {
        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_SELECT_1000_SQL));
        PreparedStatement statement3 = connectionList.get(2).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_SELECT_1000_SQL));

        /* Execute in the following order 2, 3, 1 */
        Thread thread2 = getNewThread(statement2);
        thread2.start();
        Thread.sleep(1000);
        Thread thread3 = getNewThread(statement3);
        thread3.start();
        Thread.sleep(1000);
        Thread thread1 = getNewThread(statement1);
        thread1.start();
        Thread.sleep(1000);

        Process rootProcess = testDbc.getOnlyBlockedProcessTree(true);

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

        thread1.interrupt();
        thread3.interrupt();
        thread2.interrupt();
    }

    @Test
    public void testReproWaitingLocks() throws IOException, SQLException, InterruptedException {
        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_SELECT_SLEEP_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_CREATE_INDEX_SQL));

        /* Execute in the following order 1, 2 */
        Thread thread1 = getNewThread(statement1);
        thread1.start();
        Thread.sleep(1000);
        Thread thread2 = getNewThread(statement2);
        thread2.start();
        Thread.sleep(1000);

        Process rootProcess = testDbc.getOnlyBlockedProcessTree(true);
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

        thread2.interrupt();
        thread1.interrupt();
    }

    @Test
    public void testOrderedLocks() throws IOException, SQLException, InterruptedException {
        /* create rule */
        testDbc.getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(CREATE_RULE_SQL)).execute();

        assertTrue(connectionList.get(0).getPid() < connectionList.get(1).getPid());

        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_SELECT_1000_SQL));

        /* Execute in the following order 2, 1 */
        Thread thread2 = getNewThread(statement2);
        thread2.start();
        Thread.sleep(1000);
        Thread thread1 = getNewThread(statement1);
        thread1.start();
        Thread.sleep(1000);

        Process rootProcess = testDbc.getOnlyBlockedProcessTree(true);

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

        thread1.interrupt();
        thread2.interrupt();
    }

    //TODO: remove ignore annotation and fix test after solving the issue #12290
    @Test
    @Ignore
    public void testTripleLocks() throws IOException, SQLException, InterruptedException {
        /* create rule */
        testDbc.getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(CREATE_RULE_SQL)).execute();

        PreparedStatement statement1 = connectionList.get(0).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_SELECT_1000_SQL));
        PreparedStatement statement2 = connectionList.get(1).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_DROP_RULE_SQL));
        PreparedStatement statement3 = connectionList.get(2).getConnection().prepareStatement(ProcessTreeBuilder.loadQuery(TEST_SELECT_1000_SQL));

        /* Execute in the following order 1, 2, 3 */
        Thread thread1 = getNewThread(statement1);
        thread1.start();
        Thread.sleep(1000);
        Thread thread2 = getNewThread(statement2);
        thread2.start();
        Thread.sleep(1000);
        Thread thread3 = getNewThread(statement3);
        thread3.start();
        Thread.sleep(1000);

        Process rootProcess = testDbc.getOnlyBlockedProcessTree(true);

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

        thread3.interrupt();
        thread2.interrupt();
        thread1.interrupt();
    }

    private Connection getNewConnector() throws SQLException {
        DriverManager.setLoginTimeout(10);
        return DriverManager.getConnection(
                String.format("jdbc:postgresql://%1$s:%2$s/%3$s", REMOTE_HOST, REMOTE_PORT, REMOTE_DB),
                REMOTE_USERNAME,
                REMOTE_PASSWORD);
    }

    private Thread getNewThread(PreparedStatement statement) throws SQLException {
        return new Thread(() -> {
            try {
                statement.execute();
            } catch (SQLException e) {
                LOG.warn("Statement stopped: " + statement);
            }
        }
        );
    }

    private static int getPid(Connection connection) throws SQLException {
        int pid = 0;
        ResultSet result = connection.prepareStatement(SELECT_PG_BACKEND_PID).executeQuery();
        if (result.next()) {
            pid = result.getInt(PID);
            LOG.info("Connection pid:" + pid);
        }
        return pid;
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
