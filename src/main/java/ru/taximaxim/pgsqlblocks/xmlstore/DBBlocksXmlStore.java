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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.taximaxim.pgsqlblocks.common.models.DBBlocksJournalProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBProcess;
import ru.taximaxim.pgsqlblocks.common.models.DBProcessQuery;
import ru.taximaxim.pgsqlblocks.common.models.DBProcessQueryCaller;
import ru.taximaxim.pgsqlblocks.common.models.DBProcessStatus;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;

public class DBBlocksXmlStore extends XmlStore<DBBlocksJournalProcess> {

    private static final Logger LOG = LogManager.getLogger(DBBlocksXmlStore.class);

    private static final String ROOT_TAG = "blocksJournal";

    private static final String JOURNAL_PROCESS_ROOT_ELEMENT_TAG_NAME = "journalProcess";

    private static final String CREATE_DATE_ELEMENT_TAG_NAME = "createDate";
    private static final String CLOSE_DATE_ELEMENT_TAG_NAME = "closeDate";

    private static final String PROCESS_ROOT_TAG = "process";
    private static final String CHILDREN_ELEMENT_TAG_NAME = "children";

    private static final String PID = "pid";
    private static final String BACKEND_TYPE = "backend_type";
    private static final String STATE = "state";
    private static final String STATE_CHANGE = "state_change";
    private static final String QUERY_SQL = "query";
    private static final String BACKEND_START = "backend_start";
    private static final String SLOW_QUERY = "slowQuery";
    private static final String QUERY_START = "query_start";
    private static final String XACT_START = "xact_start";
    private static final String DURATION = "duration";
    private static final String APPLICATION_NAME = "application_name";
    private static final String DAT_NAME = "datname";
    private static final String USE_NAME = "usename";
    private static final String CLIENT = "client";
    private static final String PROCESS_STATUS = "processStatus";

    private final String fileName;

    public DBBlocksXmlStore(String fileName) {
        super(ROOT_TAG);
        this.fileName = fileName;
    }

    @Override
    protected Path getXmlFile() {
        return PathBuilder.getInstance().getBlocksJournalsDir().resolve(fileName);
    }

    @Override
    protected DBBlocksJournalProcess parseElement(Node node) {
        Element element = (Element) node;
        String createDateString = element.getElementsByTagName(CREATE_DATE_ELEMENT_TAG_NAME).item(0).getTextContent();
        String closeDateString = element.getElementsByTagName(CLOSE_DATE_ELEMENT_TAG_NAME).item(0).getTextContent();
        Date createDate = DateUtils.dateFromString(createDateString);
        Date closeDate = DateUtils.dateFromString(closeDateString);
        DBProcess process = parseProcess(element.getElementsByTagName(PROCESS_ROOT_TAG).item(0));
        return new DBBlocksJournalProcess(createDate, closeDate, process);
    }

    private DBProcess parseProcess(Node node) {
        Element rootElement = (Element) node;

        int pid = Integer.parseInt(rootElement.getElementsByTagName(PID).item(0).getTextContent());
        String backendType = "";
        if (hasBackendType(rootElement)) {
            backendType = rootElement.getElementsByTagName(BACKEND_TYPE).item(0).getTextContent();
        }
        String appName = rootElement.getElementsByTagName(APPLICATION_NAME).item(0).getTextContent();
        String databaseName = rootElement.getElementsByTagName(DAT_NAME).item(0).getTextContent();
        String userName = rootElement.getElementsByTagName(USE_NAME).item(0).getTextContent();
        String client = rootElement.getElementsByTagName(CLIENT).item(0).getTextContent();
        DBProcessQueryCaller caller = new DBProcessQueryCaller(appName, databaseName, userName, client);
        String queryString = rootElement.getElementsByTagName(QUERY_SQL).item(0).getTextContent();
        boolean slowQuery = Boolean.parseBoolean(rootElement.getElementsByTagName(SLOW_QUERY).item(0).getTextContent());
        Date backendStart = DateUtils.dateFromString(rootElement.getElementsByTagName(BACKEND_START).item(0).getTextContent());
        Date queryStart = DateUtils.dateFromString(rootElement.getElementsByTagName(QUERY_START).item(0).getTextContent());
        Date xactStart = DateUtils.dateFromString(rootElement.getElementsByTagName(XACT_START).item(0).getTextContent());
        String duration = "";
        if (hasDuration(rootElement)) {
            duration = rootElement.getElementsByTagName(DURATION).item(0).getTextContent();
        }
        DBProcessQuery query = new DBProcessQuery(queryString, slowQuery, backendStart, queryStart, xactStart, duration);
        String state = rootElement.getElementsByTagName(STATE).item(0).getTextContent();
        Date stateChange = DateUtils.dateFromString(rootElement.getElementsByTagName(STATE_CHANGE).item(0).getTextContent());
        DBProcess process = new DBProcess(pid, backendType, caller, state, stateChange, query);
        Element childrenRootElement = (Element)rootElement.getElementsByTagName(CHILDREN_ELEMENT_TAG_NAME).item(0);
        NodeList childrenElements = childrenRootElement.getChildNodes();
        for (int i = 0; i < childrenElements.getLength(); i++) {
            Node childNode = childrenElements.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE
                    && childNode.getNodeName().equals(PROCESS_ROOT_TAG)) {
                process.addChild(parseProcess(childNode));
            }
        }

        String status = rootElement.getElementsByTagName(PROCESS_STATUS).item(0).getTextContent();
        process.setStatus(DBProcessStatus.getInstanceForDescr(status));

        return process;
    }

    @Override
    protected void appendChildren(Document xml, Element root, List<DBBlocksJournalProcess> list) {
        for (DBBlocksJournalProcess process : list) {
            Element rootElement = xml.createElement(JOURNAL_PROCESS_ROOT_ELEMENT_TAG_NAME);
            root.appendChild(rootElement);

            createSubElement(xml, rootElement, CREATE_DATE_ELEMENT_TAG_NAME,
                    DateUtils.dateToStringWithTz(process.getCreateDate()));
            createSubElement(xml, rootElement, CLOSE_DATE_ELEMENT_TAG_NAME,
                    DateUtils.dateToStringWithTz(process.getCloseDate()));
            appendProcess(xml, rootElement, process.getProcess());
        }
    }

    private void appendProcess(Document xml, Element root, DBProcess process) {
        Element rootElement = xml.createElement(PROCESS_ROOT_TAG);
        root.appendChild(rootElement);
        createSubElement(xml, rootElement, PID, String.valueOf(process.getPid()));
        createSubElement(xml, rootElement, BACKEND_TYPE, process.getBackendType());
        createSubElement(xml, rootElement, STATE, process.getState());
        createSubElement(xml, rootElement, STATE_CHANGE, DateUtils.dateToStringWithTz(process.getStateChange()));
        createSubElement(xml, rootElement, QUERY_SQL, process.getQuery().getQueryString());
        createSubElement(xml, rootElement, BACKEND_START, DateUtils.dateToStringWithTz(process.getQuery().getBackendStart()));
        createSubElement(xml, rootElement, SLOW_QUERY, String.valueOf(process.getQuery().isSlowQuery()));
        createSubElement(xml, rootElement, QUERY_START, DateUtils.dateToStringWithTz(process.getQuery().getQueryStart()));
        createSubElement(xml, rootElement, XACT_START, DateUtils.dateToStringWithTz(process.getQuery().getXactStart()));
        createSubElement(xml, rootElement, DURATION, process.getQuery().getDuration());
        createSubElement(xml, rootElement, APPLICATION_NAME, process.getQueryCaller().getApplicationName());
        createSubElement(xml, rootElement, DAT_NAME, process.getQueryCaller().getDatabaseName());
        createSubElement(xml, rootElement, USE_NAME, process.getQueryCaller().getUserName());
        createSubElement(xml, rootElement, CLIENT, process.getQueryCaller().getClient());
        createSubElement(xml, rootElement, PROCESS_STATUS, process.getStatus().getDescr());
        Element childrenElement = xml.createElement(CHILDREN_ELEMENT_TAG_NAME);
        rootElement.appendChild(childrenElement);
        process.getChildren().forEach(p -> appendProcess(xml, childrenElement, p));
    }

    private boolean hasBackendType(Element element) {
        return element.getElementsByTagName(BACKEND_TYPE).getLength() > 0;
    }

    private boolean hasDuration(Element element) {
        return element.getElementsByTagName(DURATION).getLength() > 0;
    }

    public static DBProcess readFromResultSet(ResultSet resultSet) throws SQLException {
        int pid = resultSet.getInt(PID);
        String backendType = hasBackendType(resultSet.getMetaData()) ? resultSet.getString(BACKEND_TYPE) : "";
        String state = resultSet.getString(STATE) == null ? "" : resultSet.getString(STATE);
        Date stateChangeDate = DateUtils.dateFromString(resultSet.getString(STATE_CHANGE));

        String queryString = resultSet.getString(QUERY_SQL);
        Date backendStart = DateUtils.dateFromString(resultSet.getString(BACKEND_START));
        Date queryStart = DateUtils.dateFromString(resultSet.getString(QUERY_START));
        Date xactStart = DateUtils.dateFromString(resultSet.getString(XACT_START));
        Duration duration = xactStart != null ? Duration.ofMillis(System.currentTimeMillis() - xactStart.getTime()) : null;
        boolean slowQuery = resultSet.getBoolean(SLOW_QUERY);

        DBProcessQuery query = new DBProcessQuery(queryString, slowQuery, backendStart, queryStart, xactStart, DateUtils.durationToString(duration));
        String appName = resultSet.getString(APPLICATION_NAME);
        String databaseName = resultSet.getString(DAT_NAME);
        String userName = resultSet.getString(USE_NAME);
        String client = resultSet.getString(CLIENT);
        DBProcessQueryCaller caller = new DBProcessQueryCaller(appName, databaseName, userName, client);

        return new DBProcess(pid, backendType, caller, state, stateChangeDate, query);
    }

    private static boolean hasBackendType(ResultSetMetaData metaData) {
        try {
            int columns = metaData.getColumnCount();
            for (int x = 1; x <= columns; x++) {
                if (BACKEND_TYPE.equals(metaData.getColumnName(x))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

}
