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

import ru.taximaxim.pgsqlblocks.PathBuilder;
import ru.taximaxim.pgsqlblocks.XmlDocumentWorker;

public final class DbcDataList {
    
    private static final Logger LOG = Logger.getLogger(DbcDataList.class);
    
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String DBNAME = "dbname";
    private static final String USER = "user";
    private static final String PASSWD = "passwd";
    private static final String ENABLED = "enabled";
    private static final String FALSE = "false";
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    
    private List<DbcData> list;
    private XmlDocumentWorker docWorker;
    
    private static DbcDataList dbcDataList;
    
    private DbcDataList() {
        File serversFile  = PathBuilder.getInstance().getServersPath().toFile();
        docWorker = new XmlDocumentWorker(serversFile);
        init();
    }
    
    public static DbcDataList getInstance() {
        if(dbcDataList == null) {
            dbcDataList = new DbcDataList();
        }
        return dbcDataList;
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
            getList().add(parseDbc(item));
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
        rootElement.item(0).appendChild(createServerElement(doc, dbcData, true));
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
    
    private DbcData parseDbc(Element item) {
        Node nameNode = item.getElementsByTagName(NAME).item(0).getFirstChild();
        Node hostNode = item.getElementsByTagName(HOST).item(0).getFirstChild();
        Node portNode = item.getElementsByTagName(PORT).item(0).getFirstChild();
        Node dbnameNode = item.getElementsByTagName(DBNAME).item(0).getFirstChild();
        Node userNode = item.getElementsByTagName(USER).item(0).getFirstChild();
        Node passwdNode = item.getElementsByTagName(PASSWD).item(0).getFirstChild();
        Node enabledNode = item.getElementsByTagName(ENABLED).item(0).getFirstChild();
        String name = nameNode == null ? "" : nameNode.getNodeValue();
        String host = hostNode == null ? "" : hostNode.getNodeValue();
        String port = portNode == null ? "" : portNode.getNodeValue();
        String dbname = dbnameNode == null ? "" : dbnameNode.getNodeValue();
        String user = userNode == null ? "" : userNode.getNodeValue();
        String passwd = passwdNode == null ? "" : passwdNode.getNodeValue();
        boolean enabled = Boolean.valueOf(enabledNode == null ? FALSE : enabledNode.getNodeValue());
        return new DbcData(name, host, port, dbname, user, passwd, enabled);
    }
    
    private void createElement(Element server, Element rows, String textContent) {
        rows.setTextContent(textContent);
        server.appendChild(rows);
    }
    
    private Element createServerElement(Document doc, DbcData dbcData, boolean wp) {
        Element server = doc.createElement(SERVER);
        createElement(server, doc.createElement(NAME), dbcData.getName());
        createElement(server, doc.createElement(HOST), dbcData.getHost());
        createElement(server, doc.createElement(PORT), dbcData.getPort());
        createElement(server, doc.createElement(USER), dbcData.getUser());
        createElement(server, doc.createElement(PASSWD), wp ? dbcData.getPasswd() : "******");
        createElement(server, doc.createElement(DBNAME), dbcData.getDbname());
        createElement(server, doc.createElement(ENABLED), String.valueOf(dbcData.isEnabled()));
        
        server.setAttribute(ID, String.valueOf(dbcData.hashCode()));
        server.setIdAttribute(ID, true);
        return server;
    }
}
