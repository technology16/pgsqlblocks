package ru.taximaxim.pgsqlblocks.common.models;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBModelSerializer {

    private static final String ROOT_ELEMENT_TAG_NAME       = "server";
    private static final String ELEMENT_NAME_TAG_NAME         = "name";
    private static final String ELEMENT_HOST_TAG_NAME         = "host";
    private static final String ELEMENT_PORT_TAG_NAME         = "port";
    private static final String ELEMENT_DATABASE_NAME_TAG_NAME = "dbname";
    private static final String ELEMENT_USER_TAG_NAME         = "user";
    private static final String ELEMENT_PASSWORD_TAG_NAME     = "passwd";
    private static final String ELEMENT_ENABLED_TAG_NAME      = "enabled";

    public DBModel deserialize(Element xmlElement) {
        Node nameNode = xmlElement.getElementsByTagName(ELEMENT_NAME_TAG_NAME).item(0);
        Node hostNode = xmlElement.getElementsByTagName(ELEMENT_HOST_TAG_NAME).item(0);
        Node portNode = xmlElement.getElementsByTagName(ELEMENT_PORT_TAG_NAME).item(0);
        Node databaseNameNode = xmlElement.getElementsByTagName(ELEMENT_DATABASE_NAME_TAG_NAME).item(0);
        Node userNode = xmlElement.getElementsByTagName(ELEMENT_USER_TAG_NAME).item(0);
        Node passwordNode = xmlElement.getElementsByTagName(ELEMENT_PASSWORD_TAG_NAME).item(0);
        Node enabledNode = xmlElement.getElementsByTagName(ELEMENT_ENABLED_TAG_NAME).item(0);

        String name = getTextContentFromNode(nameNode);
        String host = getTextContentFromNode(hostNode);
        String port = getTextContentFromNode(portNode);
        String databaseName = getTextContentFromNode(databaseNameNode);
        String user = getTextContentFromNode(userNode);
        String password = getTextContentFromNode(passwordNode);
        boolean enabled = enabledNode == null ? false : Boolean.parseBoolean(getTextContentFromNode(enabledNode));

        return new DBModel(name, host, port, databaseName, user, password, enabled);
    }

    public Element serialize(Document document, DBModel model) {
        Element rootElement = document.createElement(ROOT_ELEMENT_TAG_NAME);
        rootElement.appendChild(createElementWithContent(document, ELEMENT_NAME_TAG_NAME, model.getName()));
        rootElement.appendChild(createElementWithContent(document, ELEMENT_HOST_TAG_NAME, model.getHost()));
        rootElement.appendChild(createElementWithContent(document, ELEMENT_PORT_TAG_NAME, model.getPort()));
        rootElement.appendChild(createElementWithContent(document, ELEMENT_DATABASE_NAME_TAG_NAME, model.getDatabaseName()));
        rootElement.appendChild(createElementWithContent(document, ELEMENT_USER_TAG_NAME, model.getUser()));
        rootElement.appendChild(createElementWithContent(document, ELEMENT_PASSWORD_TAG_NAME, model.getPassword()));
        rootElement.appendChild(createElementWithContent(document, ELEMENT_ENABLED_TAG_NAME, String.valueOf(model.isEnabled())));
        return rootElement;
    }

    private String getTextContentFromNode(Node node) {
        if (node != null) {
            return node.getTextContent();
        }
        return "";
    }

    private Element createElementWithContent(Document document, String tagName, String content) {
        Element element = document.createElement(tagName);
        element.setTextContent(content);
        return element;
    }
}
