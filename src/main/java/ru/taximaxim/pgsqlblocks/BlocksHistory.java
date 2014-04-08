package ru.taximaxim.pgsqlblocks;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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

public class BlocksHistory {

    private static BlocksHistory bh;
    private static Logger log = Logger.getLogger(BlocksHistory.class);
    private ConcurrentHashMap<DbcData, List<Process>> hm = new ConcurrentHashMap<DbcData, List<Process>>();
    private ConcurrentHashMap<DbcData, List<Process>> ohm = new ConcurrentHashMap<DbcData, List<Process>>();
    private static final String filePath = "BlocksHistory";
    private static final String fileName = "/blocksHistory";


    public static BlocksHistory getInstance() {
        if(bh == null)
            bh = new BlocksHistory();
        return bh;
    }

    private BlocksHistory() {
        File dir = new File(filePath);
        if(!dir.isDirectory())
            dir.mkdir();
    }

    public void add(DbcData dbc, Process process) {
        List<Process> list = hm.get(dbc);
        if(list == null) {
            list = new ArrayList<Process>();
            hm.put(dbc, list);
            list = hm.get(dbc);
        }
        if(list.contains(process))
            return;
        list.add(process);
    }

    public synchronized void save() {
        if(hm.size() == 0){
            log.info("Не найдено блокировок для сохранения");
            return;
        }
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("servers");
            for(Entry<DbcData, List<Process>> map : hm.entrySet()) {
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
            DbcDataList.getInstance().save(doc, String.format("%s%s-%s.xml", filePath,fileName,dateTime));
        } catch (ParserConfigurationException e) {
            log.error(e);
        }
        hm.clear();
    }

    private Element createProcessElement(Document doc, Process process) {
        Element procEl = doc.createElement("process");

        Element pid = doc.createElement("pid");
        pid.setTextContent(String.valueOf(process.getPid()));
        procEl.appendChild(pid);

        Element applicationName = doc.createElement("applicationName");
        applicationName.setTextContent(process.getApplicationName());
        procEl.appendChild(applicationName);

        Element datname = doc.createElement("datname");
        datname.setTextContent(process.getDatname());
        procEl.appendChild(datname);

        Element usename = doc.createElement("usename");
        usename.setTextContent(process.getUsename());
        procEl.appendChild(usename);

        Element client = doc.createElement("client");
        client.setTextContent(process.getClient());
        procEl.appendChild(client);

        Element backendStart = doc.createElement("backendStart");
        backendStart.setTextContent(process.getBackendStart());
        procEl.appendChild(backendStart);

        Element queryStart = doc.createElement("queryStart");
        queryStart.setTextContent(process.getQueryStart());
        procEl.appendChild(queryStart);

        Element xactStart = doc.createElement("xactStart");
        xactStart.setTextContent(process.getXactStart());
        procEl.appendChild(xactStart);

        Element state = doc.createElement("state");
        state.setTextContent(process.getState());
        procEl.appendChild(state);

        Element stateChange = doc.createElement("stateChange");
        stateChange.setTextContent(process.getStateChange());
        procEl.appendChild(stateChange);

        Element blockedBy = doc.createElement("blockedBy");
        blockedBy.setTextContent(String.valueOf(process.getBlockedBy()));
        procEl.appendChild(blockedBy);

        Element query = doc.createElement("query");
        query.setTextContent(process.getQuery());
        procEl.appendChild(query);

        Element slowQuery = doc.createElement("slowQuery");
        slowQuery.setTextContent(String.valueOf(process.isSlowQuery()));
        procEl.appendChild(slowQuery);

        Element children = doc.createElement("children");
        for(Process childProcess : process.getChildren()) {
            children.appendChild(createProcessElement(doc, childProcess));
        }
        procEl.appendChild(children);
        return procEl;
    }

    public void open(String path) {
        ohm.clear();
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
                log.error("Ошибка при загрузке истории блокировок: " + path);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        if(doc == null)
            return;
        NodeList items = doc.getElementsByTagName("server");
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
                children = (NodeList)xp.evaluate("process", el, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
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
            ohm.put(dbc, list);
        }
        MainForm.getInstance().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                MainForm.getInstance().setHistoryMap(ohm);
            }
        });
    }
    
    public ConcurrentHashMap<DbcData, List<Process>> getHistoryMap() {
        return ohm;
    }
    
    private Process parseProcess(Element el) {
        Process process = new Process(
                Integer.parseInt(getNodeValue(el,"pid")),
                getNodeValue(el,"applicationName"),
                getNodeValue(el,"datname"),
                getNodeValue(el,"usename"),
                getNodeValue(el,"client"),
                getNodeValue(el,"backendStart"),
                getNodeValue(el,"queryStart"),
                getNodeValue(el,"xactStart"),
                getNodeValue(el,"state"),
                getNodeValue(el,"stateChange"),
                Integer.parseInt(getNodeValue(el,"blockedBy")),
                getNodeValue(el,"query"),
                Boolean.parseBoolean(getNodeValue(el,"slowQuery"))
                );
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        NodeList children = null;
        try {
            children = (NodeList)xp.evaluate("children/process", el, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
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
