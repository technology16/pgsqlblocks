package ru.taximaxim.pgsqlblocks;

import java.io.File;
import java.io.IOException;
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

import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcStatus;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessStatus;
import ru.taximaxim.pgsqlblocks.process.ProcessTreeBuilder;

/**
 * Класс для работы с историей блокировок
 * 
 * @author ismagilov_mg
 */
public final class BlocksHistory {
    
    private static final Logger LOG = Logger.getLogger(BlocksHistory.class);
    
    private static final String SERVERS = "servers";
    private static final String SERVER = "server";
    private static final String PROCESS = "process";
    private static BlocksHistory instance;
    
    private XmlDocumentWorker docWorker;
    private DbcDataParcer dbcDataParcer = new DbcDataParcer();
    private ProcessParcer processParcer = new ProcessParcer();
    
    public static BlocksHistory getInstance() {
        if(instance == null) {
            instance = new BlocksHistory();
        }
        synchronized (instance) {
            return instance;
        }
    }
    
    private BlocksHistory() {
        File blockHistoryFile  = PathBuilder.getInstance().getBlockHistoryPath().toFile();
        docWorker = new XmlDocumentWorker(blockHistoryFile);
    }
    
    public synchronized void save(ConcurrentMap<DbcData, ProcessTreeBuilder> processTreeMap) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(SERVERS);
            for(Entry<DbcData, ProcessTreeBuilder> map : processTreeMap.entrySet()) {
                if (map.getKey().getStatus() == DbcStatus.BLOCKED) {
                    Element server = dbcDataParcer.createServerElement(doc, map.getKey(), false);
                    for(Process process : map.getValue().getProcessTree().getChildren()) {
                        if (process.hasChildren()) {
                            server.appendChild(processParcer.createProcessElement(doc, process));
                        }
                    }
                    rootElement.appendChild(server);
                }
            }
            doc.appendChild(rootElement);
            docWorker.save(doc, PathBuilder.getInstance().getBlockHistoryPath().toFile());
        } catch (ParserConfigurationException e) {
            LOG.error(e);
        }
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
            dbc = dbcDataParcer.parseDbc(el);
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
                proc = processParcer.parseProcess(procEl);
                
                for (Process process : proc.getChildren()) {
                    if ((process.getBlockingLocks() != 0) && (process.getBlockedBy() == 0)) {
                        process.setStatus(ProcessStatus.WAITING);
                    } else if (process.getBlockedBy() != 0) {
                        process.getParent().setStatus(ProcessStatus.BLOCKING);
                        process.setStatus(ProcessStatus.BLOCKED);
                        dbc.setStatus(DbcStatus.BLOCKED);
                    }
                }
                rootProcess.addChildren(proc);
            }
            processTreeMap.put(dbc, rootProcess);
        }
        
        return processTreeMap;
    }
}