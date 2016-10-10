package ru.taximaxim.pgsqlblocks;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

import ru.taximaxim.pgsqlblocks.dbcdata.DbcData;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcDataParcer;
import ru.taximaxim.pgsqlblocks.dbcdata.DbcStatus;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessParcer;
import ru.taximaxim.pgsqlblocks.process.ProcessStatus;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

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
    private static volatile BlocksHistory instance;
    
    private XmlDocumentWorker docWorker;
    private DbcDataParcer dbcDataParcer = new DbcDataParcer();
    private ProcessParcer processParcer = new ProcessParcer();
    
    public static BlocksHistory getInstance() {
        if(instance == null) {
            synchronized(BlocksHistory.class) {
                if(instance == null) {
                    instance=new BlocksHistory();
                }
            }
        }
        return instance;
    }

    private BlocksHistory() {
        docWorker = new XmlDocumentWorker();
    }

    public void save(List<DbcData> dbcDataList) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(SERVERS);
            dbcDataList.stream()
                    .filter(dbcData -> dbcData.getStatus() == DbcStatus.BLOCKED)
                    .forEach(dbcData -> {
                Element server = dbcDataParcer.createServerElement(doc, dbcData, false);
                dbcData.getProcessTree().getProcessTree().getChildren().stream()
                        .filter(Process::hasChildren)
                        .forEach(process -> server.appendChild(processParcer.createProcessElement(doc, process)));
                rootElement.appendChild(server);
            });
            doc.appendChild(rootElement);
            docWorker.save(doc, PathBuilder.getInstance().getBlockHistoryPath().toFile());
        } catch (ParserConfigurationException e) {
            LOG.error(e);
        }
    }
    
    public List<DbcData> open(String path) {
        List<DbcData> dbcDataList = new ArrayList<DbcData>();
        Process rootProcess = new Process();
        if(path == null) {
            return dbcDataList;
        }
        Document doc = docWorker.open(Paths.get(path).toFile());
        if(doc == null) {
            return dbcDataList;
        }
        NodeList items = doc.getElementsByTagName(SERVER);
        for(int i = 0; i < items.getLength(); i++) {
            DbcData dbc = null;
            Process proc = null;
            Node node = items.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element el = (Element) node;
            if (i == items.getLength() - 1) {
                dbc = dbcDataParcer.parseDbc(el, true);
            } else {
                dbc = dbcDataParcer.parseDbc(el, false);
            }
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            NodeList children = null;
            try {
                children = (NodeList)xp.evaluate(PROCESS, el, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                LOG.error("Ошибка XPathExpressionException: " + e.getMessage());
            }
            for(int j = 0; j < (children != null ? children.getLength() : 0); j++) {
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
                dbc.setProcess(rootProcess);
            }
            dbcDataList.add(dbc);
        }
        
        return dbcDataList;
    }
}