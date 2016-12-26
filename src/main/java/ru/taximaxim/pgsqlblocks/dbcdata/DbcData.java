package ru.taximaxim.pgsqlblocks.dbcdata;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.MainForm;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeBuilder;
import ru.taximaxim.pgsqlblocks.utils.PgPassLoader;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbcData extends UpdateProvider implements Comparable<DbcData> {

    private static final Logger LOG = Logger.getLogger(DbcData.class);
    private Settings settings = Settings.getInstance();

    private Process process;

    private String name;
    private String host;
    private String port;
    private String user;
    private String password;
    private String dbname;
    private boolean enabled;
    private DbcStatus status = DbcStatus.DISABLED;
    private boolean containBlockedProcess;
    private int backendPid;

    private Connection connection;
    private final ProcessTreeBuilder treeBuilder = new ProcessTreeBuilder(this);

    public DbcData(String name,String host, String port,String dbname, String user, String passwd, boolean enabled) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.user = user;
        this.enabled = enabled;
        this.password = passwd;

        process = treeBuilder.buildProcessTree();
    }

    public void setProcess(Process process){
        this.process = process;
    }

    public Process getProcess(){
        return process;
    }

    public String getName() {
        return name;
    }
    
    public String getHost() {
        return host;
    }
    
    public String getPort() {
        return port;
    }
    
    public String getUser() {
        return user;
    }
    
    private String getUrl() {
        return String.format("jdbc:postgresql://%1$s:%2$s/%3$s", getHost(), getPort(), getDbname());
    }
    
    public String getPass() {
        return password;
    }

    public String getDbname() {
        return dbname;
    }

    public int getBackendPid() {
        return backendPid;
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    @Override
    public String toString() {
        return String.format("DbcData [name=%1$s, host=%2$s, port=%3$s, user=%4$s, " +
                        "passwd=%5$s, dbname=%6$s, enabled=%7$s, enabled=%8$s]",
            getName(), getHost(), getPort(), getUser(), getPass(), getDbname(), isEnabled(), getBackendPid());
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        if ((object == null) || !(object instanceof DbcData)) {
            return false;
        }
        DbcData table = (DbcData) object;
        return this.getName().equals(table.getName());
    }

    public DbcStatus getStatus() {
        return status;
    }
    
    public void setStatus(DbcStatus status) {
        this.status = status;
    }
    // FIXME report connection result
    public void connect() {
        if(isConnected()) {
            LOG.info(getName() + " соединение уже создано");
            return;
        }
        try {
            String pass = (getPass() == null || getPass().isEmpty()) ? new PgPassLoader(this).getPgPass() : getPass();
            LOG.info(getName() + " Соединение...");
            DriverManager.setLoginTimeout(settings.getLoginTimeout());
            connection = DriverManager.getConnection(getUrl(), getUser(), pass);
            setStatus(DbcStatus.CONNECTED);
            LOG.info(getName() + " Соединение создано.");
        } catch (SQLException e) {
            setStatus(DbcStatus.CONNECTION_ERROR);
            LOG.error(getName() + " " + e.getMessage(), e);
        }
    }
    
    public void disconnect() {
        if(connection != null) {
            try {
                connection.close();
                connection = null;
                setStatus(DbcStatus.DISABLED);
                LOG.info(getName() + " Соединение закрыто.");
            } catch (SQLException e) {
                connection = null;
                setStatus(DbcStatus.CONNECTION_ERROR);
                LOG.error(getName() + " " + e.getMessage(), e);
            } 
        }
    }
    
    public boolean isConnected() {
        try {
            return !(connection == null || connection.isClosed());
        } catch (SQLException e) {
            LOG.error("Ошибка isConnected: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int compareTo(DbcData other) {
        int result = getName().compareTo(other.getName());
        if (result==0) {
            result = getPort().compareTo(other.getPort());
        }
        if (result==0) {
            result = getDbname().compareTo(other.getDbname());
        }
        if (result==0) {
            result = getUser().compareTo(other.getUser());
        }
        if (result==0) {
            result = getPass().compareTo(other.getPass());
        }
        if (result==0) {
            result = isEnabled() ^ other.isEnabled() ? 1 : 0;
        }

        return result;
    }

    public Process getProcessTree(boolean needUpdate) {
        Process rootProcess = needUpdate ? treeBuilder.buildProcessTree() : process;
        treeBuilder.processSort(rootProcess, MainForm.getSortColumn(), MainForm.getSortDirection());
        return rootProcess;
    }

    public Process getOnlyBlockedProcessTree(boolean needUpdate) {
        Process rootProcess = needUpdate ? treeBuilder.buildOnlyBlockedProcessTree() : process;
        treeBuilder.processSort(rootProcess, MainForm.getSortColumn(), MainForm.getSortDirection());
        return rootProcess;
    }

    public boolean hasBlockedProcess() {
        return containBlockedProcess;
    }

    public void setContainBlockedProcess(boolean containBlockedProcess) {
        this.containBlockedProcess = containBlockedProcess;
    }

    public void setBackendPid(int backendPid) {
        this.backendPid = backendPid;
    }

    void updateFields(DbcData newData) {
        this.name = newData.name;
        this.host = newData.host;
        this.port = newData.port;
        this.dbname = newData.dbname;
        this.user = newData.user;
        this.password = newData.password;
        this.enabled = newData.enabled;
    }
}
