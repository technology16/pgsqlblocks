package ru.taximaxim.pgsqlblocks.dbcdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.MainForm;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeBuilder;
import ru.taximaxim.pgsqlblocks.utils.Settings;

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
    
    private Connection connection;
    private final ProcessTreeBuilder processTree = new ProcessTreeBuilder(this);

    public DbcData(String name,String host, String port,String dbname,
            String user, String passwd, boolean enabled) {
        
        this.name = name;
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.user = user;
        this.enabled = enabled;
        this.password = passwd;
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

    /**
     * Считывание пароля из pgpass
     * @return pgpass
     */
    private String getPgPass() {
        Path pgPassPath =Paths.get(getPgPassPath());
        String pgPass = "";
        try (
                BufferedReader reader = Files.newBufferedReader(
                        pgPassPath, StandardCharsets.UTF_8)
                ) {
            String settingsLine = null;
            while ((settingsLine = reader.readLine()) != null) {
                String[] settings = settingsLine.split(":");
                if (settings.length != 5) {
                    continue;
                }
                if (settings[0].equals(host) && settings[1].equals(port) && settings[2].equals(dbname)
                        && settings[3].equals(user)) {
                    // return exact match
                    return settings[4];
                    // it's not an exact match, maybe we can find exact match in next line
                } else if (equalsSettingOrAny(settings)) {
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

    private boolean equalsSettingOrAny(String[] settings) {
        boolean result = settings[0].equals(host) || "*".equals(settings[0]);
        result = result && (settings[1].equals(port) || "*".equals(settings[1]));
        result = result && (settings[2].equals(dbname) || "*".equals(settings[2]));
        return result && (settings[3].equals(user) || "*".equals(settings[3]));
    }

    /**
     * Поиск директории файла pgpass
     * @return pgpass file path
     */
    private String getPgPassPath() {
        String os = System.getProperty("os.name").toUpperCase();
         if (os.contains("NUX")) {
            return System.getProperty("user.home") + "/.pgpass";
        } else if (os.contains("WIN")) {
             // TODO: On Microsoft Windows the file is named %APPDATA%\postgresql\pgpass.conf
//            return System.getProperty("user.home") + "\\Local Settings\\ApplicationData" + "postgresql\pgpass.conf";
            return System.getenv("APPDATA") + "\\postgresql\\pgpass.conf";
        } else if (os.contains("MAC")) {
            return System.getProperty("user.home") + "/Library/Application " + "Support";
        }
        return System.getProperty("user.dir") + "/.pgpass";
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
    
    @Override
    public String toString() {
        return String.format("DbcData [name=%1$s, host=%2$s, port=%3$s, user=%4$s, passwd=%5$s, dbname=%6$s, enabled=%7$s]", 
            getName(), getHost(), getPort(), getUser(), getPass(), getDbname(), isEnabled());
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

    public Process getProcessTree() {
        Process rootProcess = processTree.getProcessTree();
        processTree.processSort(rootProcess, MainForm.getSortColumn(), MainForm.getSortDirection());
        return rootProcess;
    }

    Process getOnlyBlockedProcessTree() {
        return processTree.getOnlyBlockedProcessTree();
    }

    public boolean hasBlockedProcess() {
        return containBlockedProcess;
    }

    public void setContainBlockedProcess(boolean containBlockedProcess) {
        this.containBlockedProcess = containBlockedProcess;
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
