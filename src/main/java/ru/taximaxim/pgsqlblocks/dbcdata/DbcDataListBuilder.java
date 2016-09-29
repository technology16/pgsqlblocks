package ru.taximaxim.pgsqlblocks.dbcdata;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.taximaxim.pgsqlblocks.MainForm;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class DbcDataListBuilder {
    
    private static final Logger LOG = Logger.getLogger(DbcDataListBuilder.class);
    
    private static final String ID = "id";
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    
    private List<DbcData> dbcDataList;
    public Map<DbcData, ScheduledFuture<?>> updaterList = new HashMap<>();
    public static Optional<ScheduledExecutorService> mainService = Optional.empty();
    private DbcDataParcer dbcDataParcer = new DbcDataParcer();
    private XmlDocumentWorker docWorker = new XmlDocumentWorker();
    private File serversFile = PathBuilder.getInstance().getServersPath().toFile();
    
    private static volatile DbcDataListBuilder instance;
    
    private DbcDataListBuilder() {
        init();
    }
    
    public static DbcDataListBuilder getInstance(Optional<ScheduledExecutorService> mainExecutorService) {
        if (mainExecutorService.isPresent())
            mainService = mainExecutorService;
        if(instance == null) {
            synchronized(DbcDataListBuilder.class) {
                if(instance == null) {
                    instance = new DbcDataListBuilder();
                }
            }
        }
        LOG.debug("Set ExecutorService in DbcDataListBuilder");
        return instance;
    }
    
    public synchronized List<DbcData> getDbcDataList() {
        if (dbcDataList == null) {
            dbcDataList = new ArrayList<DbcData>();
        }
        Collections.sort(dbcDataList);
        return dbcDataList;
    }
    
    public void init() {
        Document doc = docWorker.open(serversFile);
        if(doc == null) {
            return;
        }
        NodeList items = doc.getElementsByTagName("server");
        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element item = (Element) node;
            if (i == items.getLength() - 1) {
                getDbcDataList().add(dbcDataParcer.parseDbc(item, true));
            } else {
                getDbcDataList().add(dbcDataParcer.parseDbc(item, false));
            }
        }
        LOG.debug("Fill updaterList in DbcDataListBuilder");
        for (DbcData dbcData : getDbcDataList()) {
            addScheduledUpdater(dbcData);
        }
    }
    
    public void add(DbcData dbcData) {
        if(getDbcDataList().contains(dbcData)) {
            LOG.error("Данный сервер уже есть в конфигурационном файле");
            return;
        }
        getDbcDataList().add(dbcData);
        Collections.sort(getDbcDataList());
        if (dbcData.isEnabled()) {
            LOG.debug(MessageFormat.format("Add new dbcData \"{0}\" to updaterList",
                    dbcData.getName()));
            addScheduledUpdater(dbcData);
        }
        for (DbcData data : getDbcDataList()) {
            data.setLast(false);
        }
        dbcData.setLast(true);
        Document doc = docWorker.open(serversFile);
        NodeList rootElement = doc.getElementsByTagName(SERVERS);
        rootElement.item(0).appendChild(dbcDataParcer.createServerElement(doc, dbcData, true));
        docWorker.save(doc, serversFile);
    }

    public void edit(DbcData oldDbc, DbcData newDbc) {
        delete(oldDbc);
        add(newDbc);
        removeScheduledUpdater(oldDbc);
        if (newDbc.isEnabled()) {
            LOG.debug(MessageFormat.format("Replace old dbcData \"{0}\" by new dbcData \"{1}\" in updaterList",
                    oldDbc.getName(), newDbc.getName()));
            addScheduledUpdater(newDbc);
        } else {
            LOG.debug(MessageFormat.format("Remove dbcData \"{0}\" from updaterList",
                    oldDbc.getName()));
        }
    }
    
    public void delete(DbcData oldDbc) {
        Document doc = docWorker.open(serversFile);
        NodeList nodeList = doc.getElementsByTagName(SERVER);
        for(int i=0; i<nodeList.getLength();i++) {
            Node n = nodeList.item(i);
            NamedNodeMap attrs = n.getAttributes();
            if(attrs.getNamedItem(ID).getNodeValue().equals(String.valueOf(oldDbc.hashCode()))) {
                n.getParentNode().removeChild(n);
                break;
            }
        }
        getDbcDataList().remove(oldDbc);
        LOG.info(MessageFormat.format("Remove dbcData \"{0}\" from updaterList",
                oldDbc.getName()));
        removeScheduledUpdater(oldDbc);
        docWorker.save(doc, serversFile);
    }
    
    public int getOrderNum(DbcData dbcData) {
        for (int i = 0; i < getDbcDataList().size(); i++) {
            if (getDbcDataList().get(i).equals(dbcData)) {
                return i;
            }
        }
        return 0;
    }
    
    public DbcData getLast() {
        for (DbcData data : getDbcDataList()) {
            if (data.isLast()) {
                return data;
            }
        }
        return getDbcDataList().get(getDbcDataList().size() - 1);
    }

    /**
     * Add new dbcData to updaterList
     */
    public void addScheduledUpdater(DbcData dbcData) {
        if (mainService.isPresent() && !updaterList.containsKey(dbcData)) { // TODO: need to check status too
            LOG.debug(MessageFormat.format("updaterList.size() before add \"{0}\": {1} ...", dbcData.getName(), updaterList.size()));
            updaterList.put(dbcData,
                    mainService.get().scheduleWithFixedDelay(new DbcDataRunner(dbcData),
                            0,
                            MainForm.UPDATER_PERIOD,
                            SECONDS));
            LOG.debug(MessageFormat.format("updaterList.size() after add \"{0}\": {1} ...", dbcData.getName(), updaterList.size()));
        }
    }

    /**
     * Remove dbcData from updaterList
     */
    public void removeScheduledUpdater(DbcData dbcData) {
        if (mainService.isPresent() && updaterList.containsKey(dbcData)) {
            LOG.debug(MessageFormat.format("updaterList.size() before remove \"{0}\": {1} ...", dbcData.getName(), updaterList.size()));
            updaterList.remove(dbcData).cancel(true);
            LOG.debug(MessageFormat.format("updaterList.size() after remove \"{0}\": {1} ...", dbcData.getName(), updaterList.size()));
        }
    }
}
