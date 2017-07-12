package ru.taximaxim.pgsqlblocks.common.models;

public class DBModel implements Cloneable {

    private String name;
    private String host;
    private String port;
    private String databaseName;
    private String user;
    private String password;
    private boolean enabled;

    public DBModel(String name, String host, String port, String databaseName, String user, String password, boolean enabled) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.enabled = enabled;
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

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return !password.isEmpty();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public DBModel clone() {
        return new DBModel(this.name, this.host, this.port, this.databaseName, this.user, this.password, this.enabled);
    }

    @Override
    public String toString() {
        return "DBModel{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBModel)) return false;

        DBModel dbModel = (DBModel) o;

        if (enabled != dbModel.enabled) return false;
        if (!name.equals(dbModel.name)) return false;
        if (!host.equals(dbModel.host)) return false;
        if (!port.equals(dbModel.port)) return false;
        if (!databaseName.equals(dbModel.databaseName)) return false;
        if (!user.equals(dbModel.user)) return false;
        return password.equals(dbModel.password);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port.hashCode();
        result = 31 * result + databaseName.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
