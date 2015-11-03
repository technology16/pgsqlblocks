package ru.taximaxim.pgsqlblocks.dbcdata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.taximaxim.pgsqlblocks.DbcDataParcer;
import ru.taximaxim.pgsqlblocks.PathBuilder;
import ru.taximaxim.pgsqlblocks.XmlDocumentWorker;

public final class DbcDataListBuilder {
    
    private static final Logger LOG = Logger.getLogger(DbcDataListBuilder.class);
    
    private static final String ID = "id";
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    
    private List<DbcData> list;
    private XmlDocumentWorker docWorker;
    private DbcDataParcer dbcDataParcer = new DbcDataParcer();
    
    private static DbcDataListBuilder instance;
    
    private DbcDataListBuilder() {
        File serversFile  = PathBuilder.getInstance().getServersPath().toFile();
        docWorker = new XmlDocumentWorker(serversFile);
        init();
    }
    
    public static DbcDataListBuilder getInstance() {
        if(instance == null) {
            instance = new DbcDataListBuilder();
        }
        return instance;
    }
    
    public List<DbcData> getList() {
        if (list == null) {
            list = new ArrayList<DbcData>();
        }
        return list;
    }
    
    public void init() {
        Document doc = docWorker.open();
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
            getList().add(dbcDataParcer.parseDbc(item));
        }
    }
    
    public void add(DbcData dbcData) {
        if(getList().contains(dbcData)) {
            LOG.error("Данный сервер уже есть в конфигурационном файле");
            return;
        }
        getList().add(dbcData);
        Document doc = docWorker.open();
        NodeList rootElement = doc.getElementsByTagName(SERVERS);
        rootElement.item(0).appendChild(dbcDataParcer.createServerElement(doc, dbcData, true));
        docWorker.save(doc);
    }
    
    public void edit(DbcData oldDbc, DbcData newDbc) {
        delete(oldDbc);
        add(newDbc);
    }
    
    public void delete(DbcData oldDbc) {
        Document doc = docWorker.open();
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
        docWorker.save(doc);
    }
}
