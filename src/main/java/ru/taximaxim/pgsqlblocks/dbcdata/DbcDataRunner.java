package ru.taximaxim.pgsqlblocks.dbcdata;

import org.apache.log4j.Logger;
import ru.taximaxim.pgsqlblocks.utils.Settings;

import java.text.MessageFormat;

import static java.lang.Thread.sleep;

public class DbcDataRunner implements Runnable {
    private static final Logger LOG = Logger.getLogger(DbcDataRunner.class);
    private DbcDataListBuilder dbcDataBuilder;
    private DbcData dbcData;
    private Settings settings = Settings.getInstance();

    public DbcDataRunner(DbcData data, DbcDataListBuilder dataBuilder) {
        dbcData = data;
        dbcDataBuilder = dataBuilder;
    }

    @Override
    public void run() {
        try {
            if (!dbcData.isConnected()) {
                LOG.debug(MessageFormat.format("  Connecting \"{0}\"...", dbcData.getName()));
                dbcData.connect();
            }
            if (dbcData.getStatus() == DbcStatus.CONNECTION_ERROR) {
                LOG.warn(MessageFormat.format("  Error on DbcData: {0}", dbcData.getName()));
                dbcDataBuilder.removeScheduledUpdater(dbcData);
            } else {
                dbcData.setUpdateState(true);
                LOG.info(MessageFormat.format("  Updating \"{0}\"...", dbcData.getName()));
                if (settings.isOnlyBlocked()) {
                    dbcData.setProcess(dbcData.getOnlyBlockedProcessTree(true));
                } else {
                    dbcData.setProcess(dbcData.getProcessTree(true));
                }
            }
        } catch (Exception e) {
            LOG.error(MessageFormat.format("  Error on connect or update DbcData: {0}", e.getMessage()));
        }
        LOG.debug(MessageFormat.format("  Finish updating \"{0}\"...", dbcData.getName()));
        dbcData.setUpdateState(false);
        dbcData.notifyUpdated();
    }
}
