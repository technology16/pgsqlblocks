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

public class DbcData {
    
    private static final Logger LOG = Logger.getLogger(DbcData.class);
    
    private String name;
    private String host;
    private String port;
    private String user;
    private String password;
    private String dbname;
    private boolean enabled;
    private DbcStatus status = DbcStatus.DISABLED;
    
    private Connection connection;
    
    public DbcData(String name,String host, String port,String dbname,
            String user, String passwd, boolean enabled) {
        
        this.name = name;
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.user = user;
        this.enabled = enabled;
        this.password = passwd;
        // Считывание пароля из ./pgpass
        if (passwd == null || passwd.isEmpty()) {
            try (
                    BufferedReader reader = Files.newBufferedReader(
                            Paths.get(System.getProperty("user.home") + "/.pgpass"), StandardCharsets.UTF_8);
                    ) {

                String settingsLine = null;
                while ((settingsLine = reader.readLine()) != null) {
                    String[] settings = settingsLine.split(":");
                    if (settings[0].equals(host) && (settings[1].equals(port)
                            && settings[3].equals(user))) {
                        this.password = settings[4];
                    }
                }
            } catch (FileNotFoundException e1) {
                LOG.error("Файл ./pgpass не найден");
            } catch (IOException e1) {
                LOG.error("Ошибка чтения файла ./pgpass");
            }
        }
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
    
    public String getPasswd() {
        return password;
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
            getName(), getHost(), getPort(), getUser(), getPasswd(), getDbname(), isEnabled());
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DbcData other = (DbcData) obj;
        if (getDbname() == null) {
            if (other.getDbname() != null) {
                return false;
            }
        } else if (!getDbname().equals(other.getDbname())) {
            return false;
        }
        if (getHost() == null) {
            if (other.getHost() != null){
                return false;
            }
        } else if (!getHost().equals(other.getHost())){
            return false;
        }
        if (getPort() == null) {
            if (other.getPort() != null){
                return false;
            }
        } else if (!getPort().equals(other.getPort())){
            return false;
        }
        if (getUser() == null) {
            if (other.getUser() != null){
                return false;
            }
        } else if (!getUser().equals(other.getUser())){
            return false;
        }
        return true;
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
            LOG.info(getName() + " Соединение...");
            connection = DriverManager.getConnection(getUrl(), getUser(), getPasswd());
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
}

