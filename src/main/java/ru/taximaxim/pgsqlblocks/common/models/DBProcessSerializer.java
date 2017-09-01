package ru.taximaxim.pgsqlblocks.common.models;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DBProcessSerializer {

    private static final Logger LOG = Logger.getLogger(DBProcessSerializer.class);

    private static final String ROOT_ELEMENT_TAG_NAME = "process";
    private static final String CHILDREN_ELEMENT_TAG_NAME = "children";
    private static final String PROCESS_STATUS_ELEMENT_TAG_NAME = "processStatus";

    private static final String PID = "pid";
    private static final String STATE = "state";
    private static final String STATE_CHANGE = "state_change";
    private static final String QUERY_SQL = "query";
    private static final String BACKEND_START = "backend_start";
    private static final String SLOW_QUERY = "slowQuery";
    private static final String QUERY_START = "query_start";
    private static final String XACT_START = "xact_start";
    private static final String APPLICATION_NAME = "application_name";
    private static final String DAT_NAME = "datname";
    private static final String USE_NAME = "usename";
    private static final String CLIENT = "client";

    public DBProcess deserialize(ResultSet resultSet) throws SQLException {
        int pid = resultSet.getInt(PID);
        String state = resultSet.getString(STATE);
        Date stateChangeDate = DateUtils.dateFromString(resultSet.getString(STATE_CHANGE));

        String queryString = resultSet.getString(QUERY_SQL);
        Date backendStart = DateUtils.dateFromString(resultSet.getString(BACKEND_START));
        Date queryStart = DateUtils.dateFromString(resultSet.getString(QUERY_START));
        Date xactStart = DateUtils.dateFromString(resultSet.getString(XACT_START));

        boolean slowQuery = resultSet.getBoolean(SLOW_QUERY);

        DBProcessQuery query = new DBProcessQuery(queryString, slowQuery, backendStart, queryStart, xactStart);
        String appName = resultSet.getString(APPLICATION_NAME);
        String databaseName = resultSet.getString(DAT_NAME);
        String userName = resultSet.getString(USE_NAME);
        String client = resultSet.getString(CLIENT);
        DBProcessQueryCaller caller = new DBProcessQueryCaller(appName, databaseName, userName, client);

        return new DBProcess(pid, caller, state, stateChangeDate, query);
    }

    public DBProcess deserialize(Element xmlElement, boolean elementIsRoot) {
        Element rootElement;
        if (elementIsRoot) {
            rootElement = xmlElement;
        } else {
            rootElement = (Element) xmlElement.getElementsByTagName(ROOT_ELEMENT_TAG_NAME).item(0);
        }
        int pid = Integer.valueOf(rootElement.getElementsByTagName(PID).item(0).getTextContent());
        String appName = rootElement.getElementsByTagName(APPLICATION_NAME).item(0).getTextContent();
        String databaseName = rootElement.getElementsByTagName(DAT_NAME).item(0).getTextContent();
        String userName = rootElement.getElementsByTagName(USE_NAME).item(0).getTextContent();
        String client = rootElement.getElementsByTagName(CLIENT).item(0).getTextContent();
        DBProcessQueryCaller caller = new DBProcessQueryCaller(appName, databaseName, userName, client);

        String queryString = rootElement.getElementsByTagName(QUERY_SQL).item(0).getTextContent();
        boolean slowQuery = Boolean.valueOf(rootElement.getElementsByTagName(SLOW_QUERY).item(0).getTextContent());
        Date backendStart = DateUtils.dateFromString(rootElement.getElementsByTagName(BACKEND_START).item(0).getTextContent());
        Date queryStart = DateUtils.dateFromString(rootElement.getElementsByTagName(QUERY_START).item(0).getTextContent());
        Date xactStart = DateUtils.dateFromString(rootElement.getElementsByTagName(XACT_START).item(0).getTextContent());

        DBProcessQuery query = new DBProcessQuery(queryString, slowQuery, backendStart, queryStart, xactStart);

        String state = rootElement.getElementsByTagName(STATE).item(0).getTextContent();
        Date stateChange = DateUtils.dateFromString(rootElement.getElementsByTagName(STATE_CHANGE).item(0).getTextContent());
        DBProcess process = new DBProcess(pid, caller, state, stateChange, query);
        Element childrenRootElement = (Element)rootElement.getElementsByTagName(CHILDREN_ELEMENT_TAG_NAME).item(0);
        NodeList childrenElements = childrenRootElement.getElementsByTagName(ROOT_ELEMENT_TAG_NAME);
        for (int i = 0; i < childrenRootElement.getChildNodes().getLength(); i++) {
            Node childNode = childrenElements.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)childNode;
                DBProcess childProcess = deserialize(childElement, true);
                process.addChild(childProcess);
            }
        }

        String processStatusDescr = rootElement.getElementsByTagName(PROCESS_STATUS_ELEMENT_TAG_NAME).item(0).getTextContent();
        DBProcessStatus processStatus = DBProcessStatus.getInstanceForDescr(processStatusDescr);
        process.setStatus(processStatus);

        return process;
    }

    public Element serialize(Document document, DBProcess process) {
        Element rootElement = document.createElement(ROOT_ELEMENT_TAG_NAME);
        createAndAppendElement(document, rootElement, PID, String.valueOf(process.getPid()));
        createAndAppendElement(document, rootElement, STATE, process.getState());
        createAndAppendElement(document, rootElement, STATE_CHANGE, DateUtils.dateToStringWithTz(process.getStateChange()));
        createAndAppendElement(document, rootElement, QUERY_SQL, process.getQuery().getQueryString());
        createAndAppendElement(document, rootElement, BACKEND_START, DateUtils.dateToStringWithTz(process.getQuery().getBackendStart()));
        createAndAppendElement(document, rootElement, SLOW_QUERY, String.valueOf(process.getQuery().isSlowQuery()));
        createAndAppendElement(document, rootElement, QUERY_START, DateUtils.dateToStringWithTz(process.getQuery().getQueryStart()));
        createAndAppendElement(document, rootElement, XACT_START, DateUtils.dateToStringWithTz(process.getQuery().getXactStart()));
        createAndAppendElement(document, rootElement, APPLICATION_NAME, process.getQueryCaller().getApplicationName());
        createAndAppendElement(document, rootElement, DAT_NAME, process.getQueryCaller().getDatabaseName());
        createAndAppendElement(document, rootElement, USE_NAME, process.getQueryCaller().getUserName());
        createAndAppendElement(document, rootElement, CLIENT, process.getQueryCaller().getClient());
        createAndAppendElement(document, rootElement, PROCESS_STATUS_ELEMENT_TAG_NAME, process.getStatus().getDescr());
        Element childrenElement = document.createElement(CHILDREN_ELEMENT_TAG_NAME);
        process.getChildren().forEach(childProcess -> childrenElement.appendChild(serialize(document, childProcess)));
        rootElement.appendChild(childrenElement);
        return rootElement;
    }

    private void createAndAppendElement(Document document, Element parentElement, String elementTagName, String elementContent) {
        Element element = document.createElement(elementTagName);
        element.setTextContent(elementContent);
        parentElement.appendChild(element);
    }
}
