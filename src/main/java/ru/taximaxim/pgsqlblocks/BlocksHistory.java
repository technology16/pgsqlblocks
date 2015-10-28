package ru.taximaxim.pgsqlblocks;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Класс для работы с историей блокировок
 * 
 * @author ismagilov_mg
 */
public final class BlocksHistory {
    
    private static final Logger LOG = Logger.getLogger(BlocksHistory.class);
    
    private static final String PID = "pid";
    private static final String APPLICATIONNAME = "applicationName";
    private static final String DATNAME = "datname";
    private static final String USENAME = "usename";
    private static final String CLIENT = "client";
    private static final String BACKENDSTART = "backendStart";
    private static final String QUERYSTART = "queryStart";
    private static final String XACTSTART = "xactStart";
    private static final String STATE = "state";
    private static final String STATECHANGE = "stateChange";
    private static final String BLOCKED = "blocked";
    private static final String WAITING = "waiting";
    private static final String QUERY = "query";
    private static final String SLOWQUERY = "slowQuery";
    
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    private static final String PROCESS = "process";
    private static final String CHILDREN = "children";
    private static final String FILEPATH = "BlocksHistory";
    private static final String FILENAME = "/blocksHistory";
    
    private static BlocksHistory blocksHistory;
    
    private ConcurrentMap<DbcData, List<Process>> historyMap;
    private ConcurrentMap<DbcData, List<Process>> oldHistoryMap;
    
    private Process rootBHProcess;
    private Process oldRootBHProcess;
    
    public static BlocksHistory getInstance() {
        if(blocksHistory == null) {
            blocksHistory = new BlocksHistory();
        }
        synchronized (blocksHistory) {
            return blocksHistory;
        }
    }
    
   /* public ConcurrentMap<DbcData, List<Process>> getHistoryMap() {
        if(historyMap == null){
            historyMap = new ConcurrentHashMap<DbcData, List<Process>>();
        }
        synchronized (historyMap) {
            return historyMap;
        }
    }
    
    public void clearHistoryMap() {
        if (historyMap != null) {
            historyMap.clear();
        }
    }
    
    public void clearOldHistoryMap() {
        if (oldHistoryMap != null) {
            oldHistoryMap.clear();
        }
    }
    
    public ConcurrentMap<DbcData, List<Process>> getOldHistoryMap() {
        if(oldHistoryMap == null) {
            oldHistoryMap = new ConcurrentHashMap<DbcData, List<Process>>();
        }
        synchronized (oldHistoryMap) {
            return oldHistoryMap;
        }
    }*/
    
    private BlocksHistory() {
        File dir = new File(FILEPATH);
        if(!dir.isDirectory()) {
            dir.mkdir();
        }
    }
    
   /* public void add(DbcData dbc, Process process) {
        List<Process> list = getHistoryMap().get(dbc);
        if(list == null) {
            list = new ArrayList<Process>();
            getHistoryMap().put(dbc, list);
            list = getHistoryMap().get(dbc);
        }
        if(list.contains(process)) {
            return;
        }
        list.add(process);
    }*/
    
    public synchronized void save(ConcurrentMap<DbcData, ProcessTree> processTreeMap) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(SERVERS);
            for(Entry<DbcData, ProcessTree> map : processTreeMap.entrySet()) {
                if (map.getKey().getStatus() == DbcStatus.BLOCKED) {
                    Element server = DbcDataList.getInstance().createServerElement(doc, map.getKey(), false);
                    for(Process process : map.getValue().getProcessTree().getChildren()) {
                        if (process.hasChildren()) {
                            server.appendChild(createProcessElement(doc, process));
                        }
                    }
                    rootElement.appendChild(server);
                }
            }
            doc.appendChild(rootElement);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
            Date time = new Date(System.currentTimeMillis());
            String dateTime = sdf.format(time);
            DbcDataList.getInstance().save(doc, String.format("%s%s-%s.xml", FILEPATH, FILENAME, dateTime));
        } catch (ParserConfigurationException e) {
            LOG.error(e);
        }
    }
    
    private void createElement(Element procEl, Element rows, String textContent){
        rows.setTextContent(textContent);
        procEl.appendChild(rows);
    }
    
    private Element createProcessElement(Document doc, Process process) {
        Element procEl = doc.createElement(PROCESS);
        createElement(procEl, doc.createElement(PID), String.valueOf(process.getPid()));
        createElement(procEl, doc.createElement(APPLICATIONNAME), process.getApplicationName());
        createElement(procEl, doc.createElement(DATNAME), process.getDatname());
        createElement(procEl, doc.createElement(USENAME), process.getUsename());
        createElement(procEl, doc.createElement(CLIENT), process.getClient());
        createElement(procEl, doc.createElement(BACKENDSTART), process.getBackendStart());
        createElement(procEl, doc.createElement(QUERYSTART), process.getQueryStart());
        createElement(procEl, doc.createElement(XACTSTART), process.getXactStart());
        createElement(procEl, doc.createElement(STATE), process.getState());
        createElement(procEl, doc.createElement(STATECHANGE), process.getStateChange());
        createElement(procEl, doc.createElement(BLOCKED), String.valueOf(process.getBlockedBy()));
        createElement(procEl, doc.createElement(WAITING), String.valueOf(process.getBlockingLocks()));
        createElement(procEl, doc.createElement(QUERY), process.getQuery());
        createElement(procEl, doc.createElement(SLOWQUERY), String.valueOf(process.isSlowQuery()));
        
        Element children = doc.createElement(CHILDREN);
        for(Process childProcess : process.getChildren()) {
            children.appendChild(createProcessElement(doc, childProcess));
        }
        procEl.appendChild(children);
        return procEl;
    }
    
    public ConcurrentMap<DbcData, Process> open(String path) {
        ConcurrentMap<DbcData, Process> processTreeMap = new ConcurrentHashMap<DbcData, Process>();
        Process rootProcess = new Process();
        if(path == null) {
            return processTreeMap;
        }
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder db = df.newDocumentBuilder();
            try {
                doc = db.parse(new File(path));
            } catch (SAXException | IOException e) {
                LOG.error("Ошибка при загрузке истории блокировок: " + path);
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Ошибка ParserConfigurationException: " + e.getMessage());
        }
        if(doc == null) {
            return processTreeMap;
        }
        NodeList items = doc.getElementsByTagName(SERVER);
        for(int i=0;i<items.getLength();i++) {
            DbcData dbc = null;
            Process proc = null;
            Node node = items.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element el = (Element) node;
            dbc = DbcDataList.getInstance().parseDbc(el);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            NodeList children = null;
            try {
                children = (NodeList)xp.evaluate(PROCESS, el, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                LOG.error("Ошибка XPathExpressionException: " + e.getMessage());
            }
            for(int j = 0; j < children.getLength(); j++) {
                Node processNode = children.item(j);
                if (processNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element procEl = (Element)processNode;
                proc = parseProcess(procEl);
                rootProcess.addChildren(proc);
            }
            
            processTreeMap.put(dbc, rootProcess);
        }
        
        return processTreeMap;
    }
    
    private Process parseProcess(Element el) {
        Process process = new Process(
                Integer.parseInt(getNodeValue(el, PID)),
                getNodeValue(el, APPLICATIONNAME),
                getNodeValue(el, DATNAME),
                getNodeValue(el, USENAME),
                getNodeValue(el, CLIENT),
                getNodeValue(el, BACKENDSTART),
                getNodeValue(el, QUERYSTART),
                getNodeValue(el, XACTSTART),
                getNodeValue(el, STATE),
                getNodeValue(el, STATECHANGE),
                Integer.parseInt(getNodeValue(el, BLOCKED)),
                Integer.parseInt(getNodeValue(el, WAITING)),
                getNodeValue(el, QUERY),
                Boolean.parseBoolean(getNodeValue(el, SLOWQUERY))
                );
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        NodeList children = null;
        try {
            children = (NodeList)xp.evaluate("children/process", el, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            LOG.error("Ошибка XPathExpressionException: " + e.getMessage());
        }
        for(int i=0;i<children.getLength();i++) {
            Node processNode = children.item(i);
            if (processNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element procEl = (Element)processNode;
            Process proc = parseProcess(procEl);
            proc.setParent(process);
            process.addChildren(proc);
        }
        return process;
    }
    
    private String getNodeValue(Element el, String nodeName) {
        Node node = el.getElementsByTagName(nodeName).item(0).getFirstChild();
        if(node == null) {
            return "";
        }
        return node.getNodeValue();
    }
}