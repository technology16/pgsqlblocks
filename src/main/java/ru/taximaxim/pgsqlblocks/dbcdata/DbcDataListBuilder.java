package ru.taximaxim.pgsqlblocks.dbcdata;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TableViewer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.Settings;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class DbcDataListBuilder {
    
    private static final Logger LOG = Logger.getLogger(DbcDataListBuilder.class);
    
    private static final String ID = "id";
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    
    private List<DbcData> dbcDataList;
    private Map<DbcData, ScheduledFuture<?>> updaterList = new HashMap<>();
    //private static TableViewer caServersTable;
    private static ScheduledExecutorService mainService;
    private DbcDataParcer dbcDataParcer = new DbcDataParcer();
    private XmlDocumentWorker docWorker = new XmlDocumentWorker();
    private File serversFile = PathBuilder.getInstance().getServersPath().toFile();
    private Settings settings = Settings.getInstance();
    
    private static volatile DbcDataListBuilder instance;
    
    private DbcDataListBuilder() {
        init();
    }

    public static DbcDataListBuilder getInstance(ScheduledExecutorService mainExecutorService) {
        mainService = mainExecutorService;
        if(instance == null) {
            synchronized(DbcDataListBuilder.class) {
                if(instance == null) {
                    instance = new DbcDataListBuilder();
                }
            }
        }
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
        if (settings.isAutoUpdate()) {
            getDbcDataList().stream().filter(DbcData::isEnabled).forEach(this::addScheduledUpdater);
        }
    }
    
    public void add(DbcData dbcData) {
        if(getDbcDataList().contains(dbcData)) {
            LOG.error("Данный сервер уже есть в конфигурационном файле");
            return;
        }
        getDbcDataList().add(dbcData);
        Collections.sort(getDbcDataList());
        if (settings.isAutoUpdate()) {
            addScheduledUpdater(dbcData);
        } else if (dbcData.isEnabled()) {
            mainService.schedule(new DbcDataRunner(dbcData), 0, SECONDS);
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
        if (settings.isAutoUpdate()) {
            addScheduledUpdater(newDbc);
        } else if (newDbc.isEnabled()) {
            mainService.schedule(new DbcDataRunner(newDbc), 0, SECONDS);
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
        if (!getDbcDataList().isEmpty()) {
            for (DbcData data : getDbcDataList()) {
                if (data.isLast()) {
                    return data;
                }
            }
            return getDbcDataList().get(getDbcDataList().size() - 1);
        }
        return null;
    }

    /**
     * Add new dbcData to updaterList
     */
    public void addScheduledUpdater(DbcData dbcData) {
        if (/*mainService.isPresent() && */!updaterList.containsKey(dbcData)) {
            updaterList.put(dbcData,
                    mainService.scheduleWithFixedDelay(new DbcDataRunner(dbcData),
                            0,
                            settings.getUpdatePeriod(),
                            SECONDS));
        }
    }

    /**
     * Remove dbcData from updaterList
     */
    public void removeScheduledUpdater(DbcData dbcData) {
        if (/*mainService.isPresent() && */updaterList.containsKey(dbcData)) {
            updaterList.remove(dbcData).cancel(true);
        }
    }
}
