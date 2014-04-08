package ru.taximaxim.pgsqlblocks;


public class DbcData {

    private String name;
    private String host;
    private String port;
    private String user;
    private String passwd;
    private String dbname;
    private boolean enabled;
    private DbcStatus status = DbcStatus.DISABLED;

    public DbcData(String name,String host, String port,String dbname, String user, String passwd, boolean enabled) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.user = user;
        this.passwd = passwd;
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

    public String getUser() {
        return user;
    }

    public String getUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
    }
    
    public String getPasswd() {
        return passwd;
    }

    public String getDbname() {
        return dbname;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "DbcData [name=" + name + ", host=" + host + ", port=" + port
                + ", user=" + user + ", passwd=" + passwd + ", dbname="
                + dbname + ", enabled=" + enabled + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbname == null) ? 0 : dbname.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DbcData other = (DbcData) obj;
        if (dbname == null) {
            if (other.dbname != null)
                return false;
        } else if (!dbname.equals(other.dbname))
            return false;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

    public DbcStatus getStatus() {
        return status;
    }

    public void setStatus(DbcStatus status) {
        this.status = status;
    }
}
