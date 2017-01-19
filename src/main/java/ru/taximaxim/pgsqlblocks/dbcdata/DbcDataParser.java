package ru.taximaxim.pgsqlblocks.dbcdata;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DbcDataParser {

    private static final String NAME = "name";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String DBNAME = "dbname";
    private static final String USER = "user";
    private static final String PASSWD = "passwd";
    private static final String ENABLED = "enabled";
    private static final String FALSE = "false";
    private static final String SERVER = "server";
    
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
    
    public Element createServerElement(Document doc, DbcData dbcData, boolean wp) {
        Element server = doc.createElement(SERVER);
        createElement(server, doc.createElement(NAME), dbcData.getName());
        createElement(server, doc.createElement(HOST), dbcData.getHost());
        createElement(server, doc.createElement(PORT), dbcData.getPort());
        createElement(server, doc.createElement(USER), dbcData.getUser());
        createElement(server, doc.createElement(PASSWD), wp ? dbcData.getPass() : "******");
        createElement(server, doc.createElement(DBNAME), dbcData.getDbname());
        createElement(server, doc.createElement(ENABLED), String.valueOf(dbcData.isEnabledAutoConnect()));

        return server;
    }
    
    private void createElement(Element server, Element rows, String textContent) {
        rows.setTextContent(textContent);
        server.appendChild(rows);
    }
}
