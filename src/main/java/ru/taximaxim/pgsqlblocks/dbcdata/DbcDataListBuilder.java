package ru.taximaxim.pgsqlblocks.dbcdata;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.taximaxim.pgsqlblocks.MainForm;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

public final class DbcDataListBuilder {
    
    private static final Logger LOG = Logger.getLogger(DbcDataListBuilder.class);
    
    private static final String NAME = "name";
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    
    private List<DbcData> dbcDataList;
    private final MainForm mainForm;
    private DbcDataParser dbcDataParcer = new DbcDataParser();
    private XmlDocumentWorker docWorker = new XmlDocumentWorker();
    private File serversFile = PathBuilder.getInstance().getServersPath().toFile();

    private static volatile DbcDataListBuilder instance;


    private DbcDataListBuilder(MainForm listener) {
        this.mainForm = listener;
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
    }
    
    private void editOrDeleteNode(DbcData dbcData, String oldName, boolean delMode) {
        Document doc = docWorker.open(serversFile);
        NodeList nodeList = doc.getElementsByTagName(SERVER);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            Element el = (Element) n;
            if (el.getElementsByTagName(NAME).item(0).getTextContent().equals(oldName)) {
                if (delMode) {
                    n.getParentNode().removeChild(n);
                } else {
                    n.getParentNode().replaceChild(dbcDataParcer.createServerElement(doc, dbcData, true), n);
                }
                break;
            }
        }
        docWorker.save(doc, serversFile);
    }

    private void appendToXml(DbcData dbcData){
        Document doc = docWorker.open(serversFile);
        NodeList rootElement = doc.getElementsByTagName(SERVERS);
        rootElement.item(0).appendChild(dbcDataParcer.createServerElement(doc, dbcData, true));
        docWorker.save(doc, serversFile);
    }

    private void editInXml(DbcData dbcData, String oldName){
        editOrDeleteNode(dbcData, oldName, false);
    }

    private void deleteFromXml(DbcData dbcData){
        editOrDeleteNode(dbcData, dbcData.getName(), true);
    }

    public void add(DbcData dbcData) {
        if(getDbcDataList().contains(dbcData)) {
            LOG.error("Данный сервер уже есть в конфигурационном файле");
            return;
        }
        getDbcDataList().add(dbcData);
        dbcData.setUpdateListener(mainForm);
        if (dbcData.isEnabledAutoConnect()) {
            dbcData.startUpdater();
        }

        appendToXml(dbcData);
    }

    public void delete(DbcData oldDbc) {
        deleteFromXml(oldDbc);

        getDbcDataList().remove(oldDbc);
        oldDbc.stopUpdater();
    }

    public void edit(DbcData oldData, DbcData newData) {
        String oldName = oldData.getName();
        oldData.updateFields(newData);
        editInXml(newData, oldName);

        // add new updaters
        if (oldData.isEnabledAutoConnect()) {
            oldData.startUpdater();
        }
    }
}
