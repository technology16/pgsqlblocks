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

import ru.taximaxim.pgsqlblocks.ui.MainForm;

public final class BlocksHistory {

    private static BlocksHistory bh;
    protected static final Logger LOG = Logger.getLogger(BlocksHistory.class);
    private ConcurrentMap<DbcData, List<Process>> hm;
    private ConcurrentMap<DbcData, List<Process>> ohm;
    private static final String FILEPATH = "BlocksHistory";
    private static final String FILENAME = "/blocksHistory";

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
    private static final String BLOCKEDBY = "blockedBy";
    private static final String BLOCKING_LOCKS = "blocking_locks";
    private static final String QUERY = "query";
    private static final String SLOWQUERY = "slowQuery";
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    private static final String PROCESS = "process";
    private static final String CHILDREN = "children";


    public static BlocksHistory getInstance() {
        if(bh == null) {
            bh = new BlocksHistory();
        }
        return bh;
    }

    public ConcurrentMap<DbcData, List<Process>> getHistoryMap() {
        if(hm==null){
            hm = new ConcurrentHashMap<DbcData, List<Process>>();
        }
        return hm;
    }
    
    public ConcurrentMap<DbcData, List<Process>> getOldHistoryMap() {
        if(ohm==null){
            ohm = new ConcurrentHashMap<DbcData, List<Process>>();
        }
        return ohm;
    }
    
    private BlocksHistory() {
        File dir = new File(FILEPATH);
        if(!dir.isDirectory()) {
            dir.mkdir();
        }
    }

    public void add(DbcData dbc, Process process) {
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
    }

    public synchronized void save() {
        if(getHistoryMap().size() == 0){
            LOG.info("Не найдено блокировок для сохранения");
            return;
        }
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(SERVERS);
            for(Entry<DbcData, List<Process>> map : getHistoryMap().entrySet()) {
                Element server = DbcDataList.getInstance().createServerElement(doc, map.getKey(), false);
                for(Process process : map.getValue()) {
                    server.appendChild(createProcessElement(doc, process));
                }
                rootElement.appendChild(server);
            }
            doc.appendChild(rootElement);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
            Date time = new Date(System.currentTimeMillis());
            String dateTime = sdf.format(time);
            DbcDataList.getInstance().save(doc, String.format("%s%s-%s.xml", FILEPATH,FILENAME,dateTime));
        } catch (ParserConfigurationException e) {
            LOG.error(e);
        }
        getHistoryMap().clear();
    }

    private Element createElement(Element procEl, Element rows, String textContent){
        rows.setTextContent(textContent);
        procEl.appendChild(rows);
        return procEl;
    }
    
    private Element createProcessElement(Document doc, Process process) {
        Element procEl = doc.createElement(PROCESS);
        
        procEl = createElement(procEl, doc.createElement(PID), String.valueOf(process.getPid()));
        procEl = createElement(procEl, doc.createElement(APPLICATIONNAME), process.getApplicationName());
        procEl = createElement(procEl, doc.createElement(DATNAME), process.getDatname());
        procEl = createElement(procEl, doc.createElement(USENAME), process.getUsename());
        procEl = createElement(procEl, doc.createElement(CLIENT), process.getClient());
        procEl = createElement(procEl, doc.createElement(BACKENDSTART), process.getBackendStart());
        procEl = createElement(procEl, doc.createElement(QUERYSTART), process.getQueryStart());
        procEl = createElement(procEl, doc.createElement(XACTSTART), process.getXactStart());
        procEl = createElement(procEl, doc.createElement(STATE), process.getState());
        procEl = createElement(procEl, doc.createElement(STATECHANGE), process.getStateChange());
        procEl = createElement(procEl, doc.createElement(BLOCKEDBY), String.valueOf(process.getBlockedBy()));
        procEl = createElement(procEl, doc.createElement(QUERY), process.getQuery());
        procEl = createElement(procEl, doc.createElement(SLOWQUERY), String.valueOf(process.isSlowQuery()));

        Element children = doc.createElement(CHILDREN);
        for(Process childProcess : process.getChildren()) {
            children.appendChild(createProcessElement(doc, childProcess));
        }
        procEl.appendChild(children);
        return procEl;
    }

    public void open(String path) {
        getOldHistoryMap().clear();
        if(path == null) {
            return;
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
            return;
        }
        NodeList items = doc.getElementsByTagName(SERVER);
        for(int i=0;i<items.getLength();i++) {
            DbcData dbc = null;
            Process proc = null;
            List<Process> list = new ArrayList<Process>();
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
            for(int j=0;j<children.getLength();j++) {
                Node processNode = children.item(j);
                if (processNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element procEl = (Element)processNode;
                proc = parseProcess(procEl);
                list.add(proc);
            }
            getOldHistoryMap().put(dbc, list);
        }
        MainForm.getInstance().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                MainForm.getInstance().setHistoryMap(getOldHistoryMap());
            }
        });
    }
    
    private Process parseProcess(Element el) {
        Process process = new Process(
                Integer.parseInt(getNodeValue(el,PID)),
                getNodeValue(el,APPLICATIONNAME),
                getNodeValue(el,DATNAME),
                getNodeValue(el,USENAME),
                getNodeValue(el,CLIENT),
                getNodeValue(el,BACKENDSTART),
                getNodeValue(el,QUERYSTART),
                getNodeValue(el,XACTSTART),
                getNodeValue(el,STATE),
                getNodeValue(el,STATECHANGE),
                Integer.parseInt(getNodeValue(el,BLOCKEDBY)),
                Integer.parseInt(getNodeValue(el,BLOCKING_LOCKS)),
                getNodeValue(el,QUERY),
                Boolean.parseBoolean(getNodeValue(el,SLOWQUERY))
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
