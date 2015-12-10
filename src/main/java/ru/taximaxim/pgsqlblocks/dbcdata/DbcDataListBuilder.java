package ru.taximaxim.pgsqlblocks.dbcdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

public final class DbcDataListBuilder {
    
    private static final Logger LOG = Logger.getLogger(DbcDataListBuilder.class);
    
    private static final String ID = "id";
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    
    private List<DbcData> list;
    private DbcDataParcer dbcDataParcer = new DbcDataParcer();
    private XmlDocumentWorker docWorker = new XmlDocumentWorker();
    private File serversFile = PathBuilder.getInstance().getServersPath().toFile();
    
    private static volatile DbcDataListBuilder instance;
    
    private DbcDataListBuilder() {
        init();
    }
    
    public static DbcDataListBuilder getInstance() {
        if(instance == null) {
            synchronized(DbcDataListBuilder.class) {
                if(instance == null) {
                    instance=new DbcDataListBuilder();
                }
            }
        }
        return instance;
    }
    
    public synchronized List<DbcData> getList() {
        if (list == null) {
            list = new ArrayList<DbcData>();
        }
        Collections.sort(list);
        return list;
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
                getList().add(dbcDataParcer.parseDbc(item, true));
            } else {
                getList().add(dbcDataParcer.parseDbc(item, false));
            }
        }
        for (DbcData dbcData : getList()) {
            if (dbcData.isEnabled()) {
                dbcData.connect();
            }
        }
    }
    
    public void add(DbcData dbcData) {
        if(getList().contains(dbcData)) {
            LOG.error("Данный сервер уже есть в конфигурационном файле");
            return;
        }
        getList().add(dbcData);
        Collections.sort(getList());
        if (dbcData.isEnabled()) {
            dbcData.connect();
        }
        for (DbcData data : getList()) {
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
        getList().remove(oldDbc);
        docWorker.save(doc, serversFile);
    }
    
    public int getOrderNum(DbcData dbcData) {
        for (int i = 0; i < getList().size(); i++) {
            if (getList().get(i).equals(dbcData)) {
                return i;
            }
        }
        return 0;
    }
    
    public DbcData getLast() {
        for (DbcData data : getList()) {
            if (data.isLast()) {
                return data;
            }
        }
        return getList().get(getList().size() - 1);
    }
}
