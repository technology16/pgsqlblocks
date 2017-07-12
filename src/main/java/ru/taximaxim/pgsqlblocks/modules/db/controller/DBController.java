package ru.taximaxim.pgsqlblocks.modules.db.controller;

import org.apache.log4j.Logger;
import ru.taximaxim.pgpass.PgPass;
import ru.taximaxim.pgpass.PgPassException;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.modules.db.model.DBStatus;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DBController {

    private DBModel model;

    private List<DBProcess> processes = new ArrayList<>();

    private static final Logger LOG = Logger.getLogger(DBController.class);

    private static final String QUERY_BACKEND_PID = "select pg_backend_pid();";
    private static final String PG_BACKEND_PID = "pg_backend_pid";

    private List<DBControllerListener> listeners = new ArrayList<>();

    private Settings settings = Settings.getInstance();
    private ResourceBundle resourceBundle = settings.getResourceBundle();

    private DBStatus status = DBStatus.DISABLED;

    private int backendPid;

    private Connection connection;

    public DBController(DBModel model) {
        this.model = model;
    }

    public DBModel getModel() {
        return model.clone();
    }

    public String getConnectionUrl() {
        return String.format("jdbc:postgresql://%1$s:%2$s/%3$s", model.getHost(), model.getPort(), model.getDatabaseName());
    }

    public boolean isEnabledAutoConnection() {
        return model.isEnabled();
    }

    public void connect() {
        String password = getPassword();
        try {
            listeners.forEach(listener -> listener.dbControllerWillConnect(this));
            DriverManager.setLoginTimeout(settings.getLoginTimeout());
            connection = DriverManager.getConnection(getConnectionUrl(), model.getUser(), password);
            setBackendPid(getPgBackendPid());
            setStatus(DBStatus.CONNECTED);
            listeners.forEach(listener -> listener.dbControllerDidConnect(this));
        } catch (SQLException e) {
            setStatus(DBStatus.CONNECTION_ERROR);
            listeners.forEach(listener -> listener.dbControllerConnectionFailed(this, e));
        }
    }

    private String getPassword() {
        if (model.hasPassword()) {
            return model.getPassword();
        }
        String password = "";
        try {
            password = PgPass.get(model.getHost(), model.getPort(), model.getDatabaseName(), model.getUser());
        } catch (PgPassException e) {
            LOG.error("Ошибка получения пароля из pgpass файла " + e.getMessage(), e);
        }
        return password;
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                setStatus(DBStatus.DISABLED);
                listeners.forEach(listener -> listener.dbControllerDidDisconnect(this));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int getPgBackendPid() throws SQLException {
        try (Statement stBackendPid = connection.createStatement();
             ResultSet resultSet = stBackendPid.executeQuery(QUERY_BACKEND_PID)) {
            if (resultSet.next()) {
                return resultSet.getInt(PG_BACKEND_PID);
            }
            return 0;
        }
    }

    public int getBackendPid() {
        return backendPid;
    }

    private void setBackendPid(int backendPid) {
        this.backendPid = backendPid;
    }

    public boolean isConnected() {
        try {
            return !(connection == null || connection.isClosed());
        } catch (SQLException e) {
            LOG.error(MessageFormat.format(resourceBundle.getString("error_on_check_is_connected"), e.getMessage()));
            return false;
        }
    }

    public List<DBProcess> getProcesses() {
        return processes;
    }

    public DBStatus getStatus() {
        return status;
    }

    private void setStatus(DBStatus status) {
        this.status = status;
        listeners.forEach(listener -> listener.dbControllerStatusChanged(this, this.status));
    }

    public void addListener(DBControllerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DBControllerListener listener) {
        listeners.remove(listener);
    }

    public void startProcessesUpdater() {

    }

    public void updateProcesses() {

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBController)) return false;

        DBController that = (DBController) o;

        return model.equals(that.model);
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }
}
