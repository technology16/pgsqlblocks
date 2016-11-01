package ru.taximaxim.pgsqlblocks.utils;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by shaidullin_iz on 01.11.16.
 */
public class PgPassLoader {
    private static final Logger LOG = Logger.getLogger(PgPassLoader.class);
    public static final String REGEX = "(?<=(?<!\\\\)):|(?<=(?<!\\\\)(\\\\){2}):|(?<=(?<!\\\\)(\\\\){4}):";
    private String host;
    private String port;
    private String dbname;
    private String user;

    public PgPassLoader(DbcData dbcData) {
        this(dbcData.getHost(), dbcData.getPort(), dbcData.getDbname(),  dbcData.getUser());
    }

    public PgPassLoader(String host, String port,String dbname, String user) {
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.user = user;
    }

    /**
     * Считывание пароля из pgpass
     * @return pgpass
     */
    public String getPgPass() {
        Path pgPassPath = Paths.get(getPgPassPath());
        String pgPass = "";
        try (
                BufferedReader reader = Files.newBufferedReader(
                        pgPassPath, StandardCharsets.UTF_8)
        ) {
            String settingsLine = null;
            while ((settingsLine = reader.readLine()) != null) {
                String[] settings = settingsLine.split(REGEX);
                if (settings.length != 5 || settingsLine.startsWith("#")) {
                    continue;
                }

                if (settings[0].equals(host) && settings[1].equals(port)
                        && settings[2].equals(dbname) && settings[3].equals(user)) {
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
        if (os.contains("NUX") || os.contains("NIX") || os.contains("AIX") ) {
            return System.getProperty("user.home") + "/.pgpass";
        } else if (os.contains("WIN")) {
            return System.getenv("APPDATA") + "\\postgresql\\pgpass.conf";
        } else if (os.contains("MAC")) {
            return System.getProperty("user.home") + "/Library/Application " + "Support";
        }
        return System.getProperty("user.dir") + "/.pgpass";
    }
}
