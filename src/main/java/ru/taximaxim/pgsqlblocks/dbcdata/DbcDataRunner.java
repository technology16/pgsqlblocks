package ru.taximaxim.pgsqlblocks.dbcdata;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.MainForm;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.text.MessageFormat;

public class DbcDataRunner implements Runnable {
    private static final Logger LOG = Logger.getLogger(DbcDataRunner.class);
    private DbcData dbcData;
    private Settings settings = Settings.getInstance();

    public DbcDataRunner(DbcData data) {
        dbcData = data;
    }

    @Override
    public void run() {
        try {
            if (!dbcData.isConnected()) {
                LOG.debug(MessageFormat.format("  Connecting \"{0}\"...", dbcData.getName()));
                dbcData.connect();
            }
            if (dbcData.isConnected()) {
                dbcData.setStatus(DbcStatus.UPDATE);
                LOG.info(MessageFormat.format("  Updating \"{0}\"...", dbcData.getName()));
                if (settings.isOnlyBlocked()) {
                    dbcData.setProcess(dbcData.getOnlyBlockedProcessTree());
                } else {
                    dbcData.setProcess(dbcData.getProcessTree());
                }
            } else {
                LOG.warn(MessageFormat.format(" DbcData not connected: \"{0}\"", dbcData.getName()));
            }
        } catch (Exception e) {
            LOG.error(MessageFormat.format("  Error on connect or update DbcData: {0}", e.getMessage()));
        }
        LOG.info(MessageFormat.format("  Finish updating \"{0}\"...", dbcData.getName()));
    }
}
