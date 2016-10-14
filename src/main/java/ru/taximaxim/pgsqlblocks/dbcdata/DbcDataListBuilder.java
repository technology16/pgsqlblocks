package ru.taximaxim.pgsqlblocks.dbcdata;

import java.io.File;
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
import ru.taximaxim.pgsqlblocks.utils.Settings;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class DbcDataListBuilder {
    
    private static final Logger LOG = Logger.getLogger(DbcDataListBuilder.class);
    
    private static final String ID = "id";
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    
    private List<DbcData> dbcDataList;
    private Map<DbcData, ScheduledFuture<?>> updaterMap = new HashMap<>();
    private Map<DbcData, ScheduledFuture<?>> updateOnceMap = new HashMap<>();
    private final ScheduledExecutorService mainService;
    private final MainForm mainForm;
    private DbcDataParser dbcDataParcer = new DbcDataParser();
    private XmlDocumentWorker docWorker = new XmlDocumentWorker();
    private File serversFile = PathBuilder.getInstance().getServersPath().toFile();
    private Settings settings = Settings.getInstance();
    
    private static volatile DbcDataListBuilder instance;


    private DbcDataListBuilder(MainForm listener) {
        this.mainForm = listener;
        this.mainService = mainForm.getMainService();
        readFromFile();
    }

    public static DbcDataListBuilder getInstance(MainForm mainForm) {
        if(instance == null) {
            synchronized(DbcDataListBuilder.class) {
                if(instance == null) {
                    instance = new DbcDataListBuilder(mainForm);
                }
            }
        }
        return instance;
    }

    public synchronized List<DbcData> getDbcDataList() {
        if (dbcDataList == null) {
            dbcDataList = new ArrayList<>();
        }
        Collections.sort(dbcDataList);
        return dbcDataList;
    }
    
    private void readFromFile() {
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

            DbcData data = dbcDataParcer.parseDbc(item);
            data.setUpdateListener(mainForm);
            getDbcDataList().add(data);
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
            addOnceScheduledUpdater(dbcData);
        }
        Document doc = docWorker.open(serversFile);
        NodeList rootElement = doc.getElementsByTagName(SERVERS);
        rootElement.item(0).appendChild(dbcDataParcer.createServerElement(doc, dbcData, true));
        docWorker.save(doc, serversFile);
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
        removeOnceScheduledUpdater(oldDbc);
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

    /**
     * Add new dbcData to updaterMap
     */
    public void addScheduledUpdater(DbcData dbcData) {
        dbcData.setUpdateListener(mainForm);
        updaterMap.putIfAbsent(dbcData, 
                mainService.scheduleWithFixedDelay(new DbcDataRunner(dbcData), 0, settings.getUpdatePeriod(), SECONDS));
    }

    /**
     * Remove dbcData from updaterMap
     */
    public void removeScheduledUpdater(DbcData dbcData) {
        if (updaterMap.containsKey(dbcData)) {
            updaterMap.remove(dbcData).cancel(true);
        }
    }

    /**
     * Add new dbcData to updateOnceMap
     */
    public void addOnceScheduledUpdater(DbcData dbcData) {
        updateOnceMap.putIfAbsent(dbcData,
                mainService.schedule(new DbcDataRunner(dbcData), 0, SECONDS));
    }

    /**
     * Remove dbcData from updateOnceMap
     */
    public void removeOnceScheduledUpdater(DbcData dbcData) {
        if (updateOnceMap.containsKey(dbcData)) {
            updateOnceMap.remove(dbcData).cancel(true);
        }
    }
}
