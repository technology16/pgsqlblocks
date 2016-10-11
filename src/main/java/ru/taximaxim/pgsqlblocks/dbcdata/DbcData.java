package ru.taximaxim.pgsqlblocks.dbcdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeBuilder;
import ru.taximaxim.pgsqlblocks.utils.Settings;

public class DbcData implements Comparable<DbcData> {
    
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
    private boolean isLast;
    private boolean containBlockedProcess;
    
    private Connection connection;
    private ProcessTreeBuilder processTree = null;
    private ProcessTreeBuilder blockedProcessTree = null;

    public DbcData(String name,String host, String port,String dbname,
            String user, String passwd, boolean enabled, boolean isLast) {
        
        this.name = name;
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.user = user;
        this.enabled = enabled;
        this.password = passwd;
        this.isLast = isLast;
    }

    public ProcessTreeBuilder getProcessTree() {
        return processTree;
    }

    public void setProcessTree(ProcessTreeBuilder processTree) {
        this.processTree = processTree;
    }

    public ProcessTreeBuilder getBlockedProcessTree() {
        return blockedProcessTree;
    }

    public void setBlockedProcessTree(ProcessTreeBuilder blockedProcessTree) {
        this.blockedProcessTree = blockedProcessTree;
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
    
    public String getUrl() {
        return String.format("jdbc:postgresql://%1$s:%2$s/%3$s", getHost(), getPort(), getDbname());
    }
    
    public String getPass() {
        return password;
    }
    
    public String getPgPass() {
        // Считывание пароля из ./pgpass
        String pgPass = "";
        try (
                BufferedReader reader = Files.newBufferedReader(
                        Paths.get(System.getProperty("user.home") + "/.pgpass"), StandardCharsets.UTF_8);
                ) {

            String settingsLine = null;
            while ((settingsLine = reader.readLine()) != null) {
                String[] settings = settingsLine.split(":");
                if (settings[0].equals(host) && (settings[1].equals(port)
                        && settings[3].equals(user))) {
                    pgPass = settings[4];
                }
            }
        } catch (FileNotFoundException e1) {
            LOG.error("Файл ./pgpass не найден");
        } catch (IOException e1) {
            LOG.error("Ошибка чтения файла ./pgpass");
        }
        
        return pgPass;
    }
    
    public String getDbname() {
        return dbname;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }
    
    public boolean isLast() {
        return isLast;
    }
    
    @Override
    public String toString() {
        return String.format("DbcData [name=%1$s, host=%2$s, port=%3$s, user=%4$s, passwd=%5$s, dbname=%6$s, enabled=%7$s]", 
            getName(), getHost(), getPort(), getUser(), getPass(), getDbname(), isEnabled());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDbname() == null) ? 0 : getDbname().hashCode());
        result = prime * result + ((getHost() == null) ? 0 : getHost().hashCode());
        result = prime * result + ((getPort() == null) ? 0 : getPort().hashCode());
        result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
        return result;
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
    
    public void connect() {
        if(isConnected()) {
            LOG.info(getName() + " соединение уже создано");
            return;
        }
        try {
            String pass = (getPass() == null || getPass().isEmpty()) ? getPgPass() : getPass();
            LOG.info(getName() + " Соединение...");
            DriverManager.setLoginTimeout(settings.getLoginTimeout());
            connection = DriverManager.getConnection(getUrl(), getUser(), pass);
            setStatus(DbcStatus.CONNECTED);
            LOG.info(getName() + " Соединение создано.");
        } catch (SQLException e) {
            setStatus(DbcStatus.ERROR);
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
                setStatus(DbcStatus.ERROR);
                LOG.error(getName() + " " + e.getMessage(), e);
            } 
        }
    }
    
    public boolean isConnected() {
        try {
            if(connection == null || connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Ошибка isConnected: " + e.getMessage());
        }
        return true;
    }

    @Override
    public int compareTo(DbcData other) {
        return getName().compareTo(other.getName());
    }

    public boolean hasBlockedProcess() {
        return containBlockedProcess;
    }

    public void setContainBlockedProcess(boolean containBlockedProcess) {
        this.containBlockedProcess = containBlockedProcess;
    }
}
