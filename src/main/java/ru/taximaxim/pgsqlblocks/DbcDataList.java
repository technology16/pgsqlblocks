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



public class DbcDataList {

    private Logger log = Logger.getLogger(DbcDataList.class);
    private String filePath = "servers.xml";
    private List<DbcData> list;

    private static DbcDataList dbcDataList;

    private DbcDataList() {}

    public static DbcDataList getInstance() {
        if(dbcDataList == null)
            dbcDataList = new DbcDataList();
        return dbcDataList;
    }

    public List<DbcData> getList() {
        return list;
    }

    public void init() {
        list = new ArrayList<DbcData>();
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder db = df.newDocumentBuilder();
            try {
                doc = db.parse(new File(filePath));
            } catch (SAXException | IOException e) {
                log.error("Не найден файл конфигурации");
                createConfFile();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        if(doc == null)
            return;
        NodeList items = doc.getElementsByTagName("server");
        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element item = (Element) node;
            list.add(parseDbc(item));
        }
    }

    public DbcData parseDbc(Element item) {
        Node nameNode    = item.getElementsByTagName("name").item(0).getFirstChild();
        Node hostNode    = item.getElementsByTagName("host").item(0).getFirstChild();
        Node portNode    = item.getElementsByTagName("port").item(0).getFirstChild();
        Node dbnameNode  = item.getElementsByTagName("dbname").item(0).getFirstChild();
        Node userNode    = item.getElementsByTagName("user").item(0).getFirstChild();
        Node passwdNode  = item.getElementsByTagName("passwd").item(0).getFirstChild();
        Node enabledNode = item.getElementsByTagName("enabled").item(0).getFirstChild();
        String name   = nameNode   == null?"":nameNode.getNodeValue();
        String host   = hostNode   == null?"":hostNode.getNodeValue();
        String port   = portNode   == null?"":portNode.getNodeValue();
        String dbname = dbnameNode == null?"":dbnameNode.getNodeValue();
        String user   = userNode   == null?"":userNode.getNodeValue();
        String passwd = passwdNode == null?"":passwdNode.getNodeValue();
        boolean enabled = Boolean.valueOf(enabledNode==null?"false":enabledNode.getNodeValue());
        DbcData dbcData = new DbcData(name, host, port, dbname, user, passwd, enabled);
        return dbcData;
    }
    
    private void createConfFile() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("servers");
            doc.appendChild(rootElement);
            save(doc, filePath);
        } catch (ParserConfigurationException e) {
            log.error(e);
        }
        log.info("Создан файл конфигурации " + filePath);
    }

    public void add(DbcData dbcData) {
        if(list.contains(dbcData)) {
            log.error("Данный сервер уже есть в конфигурационном файле");
            return;
        }
        list.add(dbcData);

        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder db = df.newDocumentBuilder();
            try {
                doc = db.parse(new File(filePath));
            } catch (SAXException | IOException e) {
                log.error("Не найден файл конфигурации");
                createConfFile();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        NodeList rootElement = doc.getElementsByTagName("servers");
        rootElement.item(0).appendChild(createServerElement(doc, dbcData, true));

        save(doc, filePath);
    }

    public Element createServerElement(Document doc, DbcData dbcData, boolean wp) {
        Element server = doc.createElement("server");

        Element name = doc.createElement("name");
        name.setTextContent(dbcData.getName());
        server.appendChild(name);

        Element host = doc.createElement("host");
        host.setTextContent(dbcData.getHost());
        server.appendChild(host);

        Element port = doc.createElement("port");
        port.setTextContent(dbcData.getPort());
        server.appendChild(port);

        Element user = doc.createElement("user");
        user.setTextContent(dbcData.getUser());
        server.appendChild(user);

        Element passwd = doc.createElement("passwd");
        passwd.setTextContent(wp?dbcData.getPasswd():"******");
        server.appendChild(passwd);

        Element dbname = doc.createElement("dbname");
        dbname.setTextContent(dbcData.getDbname());
        server.appendChild(dbname);

        Element enabled = doc.createElement("enabled");
        enabled.setTextContent(String.valueOf(dbcData.isEnabled()));
        server.appendChild(enabled);
        server.setAttribute("id", String.valueOf(dbcData.hashCode()));
        server.setIdAttribute("id", true);
        return server;
    } 
    
    public void save(Document doc, String filePath){
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            try {
                transformer.transform(source, result);
            } catch (TransformerException e) {
                log.error(e);
            }
        } catch (TransformerConfigurationException e) {
            log.error(e);
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
                doc = db.parse(new File(filePath));
            } catch (SAXException | IOException e) {
                log.error("Не найден файл конфигурации", e);
                createConfFile();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        NodeList nodeList = doc.getElementsByTagName("server");
        for(int i=0; i<nodeList.getLength();i++) {
            Node n = nodeList.item(i);
            NamedNodeMap attrs = n.getAttributes();
            if(attrs.getNamedItem("id").getNodeValue().equals(String.valueOf(oldDbc.hashCode()))) {
                n.getParentNode().removeChild(n);
                break;
            }
        }
        list.remove(oldDbc);
        save(doc, filePath);
    }
}
