/*******************************************************************************
 * Copyright 2017-2024 TAXTELECOM, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.taximaxim.pgsqlblocks.xmlstore;

import java.nio.file.Path;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;

public class DBModelsXmlStore extends XmlStore<DBModel> {

    private static final String ROOT_TAG = "servers";

    private static final String ROOT_ELEMENT_TAG_NAME          = "server";
    private static final String ELEMENT_NAME_TAG_NAME          = "name";
    private static final String ELEMENT_HOST_TAG_NAME          = "host";
    private static final String ELEMENT_PORT_TAG_NAME          = "port";
    private static final String ELEMENT_DATABASE_NAME_TAG_NAME = "dbname";
    private static final String ELEMENT_DB_GROUP_NAME          = "dbgroup";
    private static final String ELEMENT_USER_TAG_NAME          = "user";
    private static final String ELEMENT_PASSWORD_TAG_NAME      = "passwd";
    private static final String ELEMENT_READ_BACKEND_TAG_NAME  = "readbt";
    private static final String ELEMENT_ENABLED_TAG_NAME       = "enabled";

    public DBModelsXmlStore() {
        super(ROOT_TAG);
    }

    @Override
    protected Path getXmlFile() {
        return PathBuilder.getInstance().getServersPath();
    }

    @Override
    protected DBModel parseElement(Node node) {
        Element element = (Element) node;
        Node nameNode = element.getElementsByTagName(ELEMENT_NAME_TAG_NAME).item(0);
        Node hostNode = element.getElementsByTagName(ELEMENT_HOST_TAG_NAME).item(0);
        Node portNode = element.getElementsByTagName(ELEMENT_PORT_TAG_NAME).item(0);
        Node databaseNameNode = element.getElementsByTagName(ELEMENT_DATABASE_NAME_TAG_NAME).item(0);
        Node dbGroupNode = element.getElementsByTagName(ELEMENT_DB_GROUP_NAME).item(0);
        Node userNode = element.getElementsByTagName(ELEMENT_USER_TAG_NAME).item(0);
        Node passwordNode = element.getElementsByTagName(ELEMENT_PASSWORD_TAG_NAME).item(0);
        Node readBackendNode = element.getElementsByTagName(ELEMENT_READ_BACKEND_TAG_NAME).item(0);
        Node enabledNode = element.getElementsByTagName(ELEMENT_ENABLED_TAG_NAME).item(0);

        String name = getTextContentFromNode(nameNode);
        String host = getTextContentFromNode(hostNode);
        String port = getTextContentFromNode(portNode);
        String databaseName = getTextContentFromNode(databaseNameNode);
        String dbGroup = getTextContentFromNode(dbGroupNode);
        String user = getTextContentFromNode(userNode);
        String password = getTextContentFromNode(passwordNode);
        boolean readBackend = readBackendNode != null && Boolean.parseBoolean(getTextContentFromNode(readBackendNode));
        boolean enabled = enabledNode != null && Boolean.parseBoolean(getTextContentFromNode(enabledNode));

        return new DBModel(name, host, port, databaseName, dbGroup, user, password, readBackend, enabled);
    }

    @Override
    protected void appendChildren(Document xml, Element root, List<DBModel> list) {
        for (DBModel model : list) {
            Element rootElement = xml.createElement(ROOT_ELEMENT_TAG_NAME);
            root.appendChild(rootElement);

            createSubElement(xml, rootElement, ELEMENT_NAME_TAG_NAME,  model.getName());
            createSubElement(xml, rootElement, ELEMENT_HOST_TAG_NAME, model.getHost());
            createSubElement(xml, rootElement, ELEMENT_PORT_TAG_NAME, model.getPort());
            createSubElement(xml, rootElement, ELEMENT_DATABASE_NAME_TAG_NAME, model.getDatabaseName());
            createSubElement(xml, rootElement, ELEMENT_DB_GROUP_NAME, model.getDbGroup());
            createSubElement(xml, rootElement, ELEMENT_USER_TAG_NAME, model.getUser());
            createSubElement(xml, rootElement, ELEMENT_PASSWORD_TAG_NAME, model.getPassword());
            createSubElement(xml, rootElement, ELEMENT_READ_BACKEND_TAG_NAME, String.valueOf(model.isReadBackendType()));
            createSubElement(xml, rootElement, ELEMENT_ENABLED_TAG_NAME, String.valueOf(model.isEnabled()));
        }
    }

    private String getTextContentFromNode(Node node) {
        if (node != null) {
            return node.getTextContent();
        }
        return "";
    }
}
