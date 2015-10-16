package ru.taximaxim.pgsqlblocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class DbcDataList {
    
    private static final Logger LOG = Logger.getLogger(DbcDataList.class);
    
    private static final String FILE_PATH = System.getProperty("user.home") + "/servers.xml";
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
    
    private static DbcDataList dbcDataList;
    
    private DbcDataList() {}
    
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
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder db = df.newDocumentBuilder();
            try {
                doc = db.parse(new File(FILE_PATH));
            } catch (SAXException | IOException e) {
                LOG.error("Не найден файл конфигурации");
                createConfFile();
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Ошибка ParserConfigurationException: " + e.getMessage());
        }
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
    
    public DbcData parseDbc(Element item) {
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
    
    private void createConfFile() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(SERVERS);
            doc.appendChild(rootElement);
            save(doc, FILE_PATH);
        } catch (ParserConfigurationException e) {
            LOG.error(e);
        }
        LOG.info("Создан файл конфигурации " + FILE_PATH);
    }
    
    public void add(DbcData dbcData) {
        if(getList().contains(dbcData)) {
            LOG.error("Данный сервер уже есть в конфигурационном файле");
            return;
        }
        getList().add(dbcData);
        
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder db = df.newDocumentBuilder();
            try {
                doc = db.parse(new File(FILE_PATH));
            } catch (SAXException | IOException e) {
                LOG.error("Не найден файл конфигурации");
                createConfFile();
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Ошибка ParserConfigurationException: " + e.getMessage());
        }
        NodeList rootElement = doc.getElementsByTagName(SERVERS);
        rootElement.item(0).appendChild(createServerElement(doc, dbcData, true));
        
        save(doc, FILE_PATH);
    }
    
    private void createElement(Element server, Element rows, String textContent) {
        rows.setTextContent(textContent);
        server.appendChild(rows);
    }
    
    public Element createServerElement(Document doc, DbcData dbcData, boolean wp) {
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
    
    public void save(Document doc, String filePath) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            try {
                transformer.transform(source, result);
            } catch (TransformerException e) {
                LOG.error(e);
            }
        } catch (TransformerConfigurationException e) {
            LOG.error(e);
        }
    }
    
    public void edit(DbcData oldDbc, DbcData newDbc) {
        delete(oldDbc);
        add(newDbc);
    }
    
    public void delete(DbcData oldDbc) {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder db = df.newDocumentBuilder();
            try {
                doc = db.parse(new File(FILE_PATH));
            } catch (SAXException | IOException e) {
                LOG.error("Не найден файл конфигурации", e);
                createConfFile();
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Ошибка ParserConfigurationException: " + e.getMessage());
        }
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
        save(doc, FILE_PATH);
    }
}
