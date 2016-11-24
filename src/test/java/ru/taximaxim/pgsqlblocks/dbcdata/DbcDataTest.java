package ru.taximaxim.pgsqlblocks.dbcdata;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.taximaxim.pgsqlblocks.TEST.*;

public class DbcDataTest {
    private static DbcData testDbc;
    private static final Logger LOG = Logger.getLogger(DbcDataTest.class);
    private static List bomberList = Collections.synchronizedList(new ArrayList());

    @BeforeClass
    public static void initialize() throws IOException {
        testDbc = new DbcData("TestDbc", REMOTE_HOST,  REMOTE_PORT, REMOTE_DB,
                REMOTE_USERNAME,  REMOTE_PASSWORD,  true);
    }

    @Before
    public void beforeTest() throws IOException, SQLException {
        if (!testDbc.isConnected()) {
            testDbc.connect();
        }
        /* prepare db */
        testDbc.getConnection().prepareStatement(loadQuery(TESTING_DUMP_SQL)).execute();
        /* create rule */
        testDbc.getConnection().prepareStatement(loadQuery(CREATE_RULE_SQL)).execute();
    }

    @Test
    public void test_multiple_locks() throws IOException, SQLException {
        Connection conn1 = getNewConnector();
        int conn1Pid = getPid(conn1);
//        bomberList.add(conn1Pid);
        LOG.info("conn1Pid:" + conn1Pid + "/" + bomberList.add(conn1Pid));
        Connection conn2 = getNewConnector();
        int conn2Pid = getPid(conn2);
//        bomberList.add(conn2Pid);
        LOG.info("conn2Pid:" + conn2Pid+ "/" + bomberList.add(conn2Pid));
        Connection conn3 = getNewConnector();
        int conn3Pid = getPid(conn3);
//        bomberList.add(conn3Pid);
        LOG.info("conn3Pid:" + conn3Pid+ "/" + bomberList.add(conn3Pid));

        PreparedStatement statement1 = conn1.prepareStatement(loadQuery(TEST_COMMON_LOCKS_1_SQL));
        PreparedStatement statement2 = conn2.prepareStatement(loadQuery(TEST_COMMON_LOCKS_2_SQL));
        PreparedStatement statement3 = conn3.prepareStatement(loadQuery(TEST_COMMON_LOCKS_2_SQL));

        /* Execute in the following order 2, 3, 1 */
        Thread thread2 = getNewThread(statement2);
        thread2.start();
        Thread thread3 = getNewThread(statement3);
        thread3.start();
        Thread thread1 = getNewThread(statement1);
        thread1.start();

        Process rootProcess = testDbc.getProcessTree(true);
        LOG.info("rootProcess:" + rootProcess);

        List<Process> allGrandChild = rootProcess.getChildren().stream().
                flatMap(l -> l.getChildren().stream()).
                collect(Collectors.toList());

        Optional <Process> proc1 = allGrandChild.stream().filter(x -> (x.getPid() == conn1Pid)).findFirst();
        Optional <Process> proc2 = rootProcess.getChildren().stream().filter(x -> (x.getPid() == conn2Pid)).findFirst();
        Optional <Process> proc3 = rootProcess.getChildren().stream().filter(x -> (x.getPid() == conn3Pid)).findFirst();


        if (!(proc1.isPresent() && proc2.isPresent() && proc3.isPresent())) throw new AssertionError();
        {

            assertEquals(ProcessStatus.BLOCKED, proc1.get().getStatus());
            assertEquals(ProcessStatus.BLOCKING, proc2.get().getStatus());
            assertEquals(ProcessStatus.BLOCKING, proc3.get().getStatus());

            assertTrue(proc2.get().hasChildren()
                    && (proc1.get().getParents().stream().anyMatch(x -> x.getPid() == proc2.get().getPid())));
            assertTrue(proc3.get().hasChildren()
                    && (proc1.get().getParents().stream().anyMatch(x -> x.getPid() ==  proc3.get().getPid())));
        }

        thread1.interrupt();
        thread3.interrupt();
        thread2.interrupt();
        executionPids();
        conn1.close();
        conn3.close();
        conn2.close();
    }

    @AfterClass
    public static void terminate() throws SQLException {
        executionPids();
        testDbc.disconnect();
    }

    private String loadQuery(String queryFile){
        try (InputStream input = ClassLoader.getSystemResourceAsStream(queryFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(System.lineSeparator());
            }
            return out.toString();
        } catch (IOException e) {
            LOG.error("Ошибка чтения файла " + queryFile, e);
            return null;
        }
    }

    private Connection getNewConnector() throws SQLException {
        DriverManager.setLoginTimeout(10);
        Connection connection = DriverManager.getConnection(
                String.format("jdbc:postgresql://%1$s:%2$s/%3$s", REMOTE_HOST, REMOTE_PORT, REMOTE_DB),
                REMOTE_USERNAME,
                REMOTE_PASSWORD);
        return connection;
    }

    private Thread getNewThread(PreparedStatement statement) throws SQLException {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    statement.execute();
                } catch (SQLException e) {
                    LOG.warn("Statement stopped:" + statement);
                }
            }}
        );
        return t;
    }

    private int getPid(Connection connection) throws SQLException {
        int pid = 0;
        ResultSet result = connection.prepareStatement(SELECT_PG_BACKEND_PID).executeQuery();
        if (result.next()) {
            pid = result.getInt(PID);
            LOG.info("Connection pid:" + pid);
        }
        return pid;
    }

    private static void executionPids() throws SQLException {
        List newList = Collections.synchronizedList(new ArrayList());
        for(Object processID : bomberList)
        {
            String prepared = PG_TERMINATE_BACKEND[0] + processID + PG_TERMINATE_BACKEND[1];
            LOG.info("Prepared query:" + prepared);
            ResultSet result = testDbc.getConnection().prepareStatement(prepared).executeQuery();
            if (result.next()) {
                boolean terminatedSuccesed = result.getBoolean(TERMINATED_SUCCESED);
                LOG.info("Terminating the process pid:" + processID + " is succeed:" + terminatedSuccesed);
                if (terminatedSuccesed) {
                    LOG.info("Process terminated:" + processID);
                } else {
                    newList.add(processID);
                }
            }
        }
        bomberList = newList;
    }
}
