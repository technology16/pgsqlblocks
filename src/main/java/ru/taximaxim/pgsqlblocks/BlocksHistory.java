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
import ru.taximaxim.pgsqlblocks.dbcdata.DbcDataParser;
import ru.taximaxim.pgsqlblocks.process.Process;
import ru.taximaxim.pgsqlblocks.process.ProcessParser;
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
    private DbcDataParser dbcDataParcer = new DbcDataParser();
    private ProcessParser processParser = new ProcessParser();
    
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
                    .filter(DbcData::hasBlockedProcess)
                    .forEach(dbcData -> {
                Element server = dbcDataParcer.createServerElement(doc, dbcData, false);
                dbcData.getProcessTree(false).getChildren().stream()
                        .filter(Process::hasChildren)
                        .forEach(process -> server.appendChild(processParser.createProcessElement(doc, process)));
                rootElement.appendChild(server);
            });
            doc.appendChild(rootElement);
            docWorker.save(doc, PathBuilder.getInstance().getBlockHistoryPath().toFile());
        } catch (ParserConfigurationException e) {
            LOG.error(e);
        }
    }
    
    public List<DbcData> open(String path) {
        List<DbcData> dbcDataList = new ArrayList<>();
        Process rootProcess = new Process(0, null,null,null,null);
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
            parseChildren(rootProcess, dbc, children);
            dbcDataList.add(dbc);
        }
        
        return dbcDataList;
    }

    void parseChildren(Process rootProcess, DbcData dbc, NodeList children) {
        for(int j = 0; j < (children != null ? children.getLength() : 0); j++) {
            Node processNode = children.item(j);
            if (processNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element procEl = (Element)processNode;
            Process proc = processParser.parseProcess(procEl);

            for (Process process : proc.getChildren()) {
                if (!process.getBlocks().isEmpty()) {
                    process.getParents().forEach(p -> p.setStatus(ProcessStatus.BLOCKING));
                    process.setStatus(ProcessStatus.BLOCKED);
                    dbc.setContainBlockedProcess(true);
                }
            }
            rootProcess.addChildren(proc);
            dbc.setProcess(rootProcess);
        }
    }
}