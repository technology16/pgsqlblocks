package ru.taximaxim.pgsqlblocks.process;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProcessParcer {
    
    private static final Logger LOG = Logger.getLogger(ProcessParcer.class);

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
    private static final String RELATION = "relation";
    private static final String LOCKTYPE = "locktype";
    private static final String QUERY = "query";
    private static final String SLOWQUERY = "slowQuery";
    private static final String PROCESS = "process";
    private static final String CHILDREN = "children";
    private static final String BLOCK = "block";
    private static final String BLOCKING_PID = "blockingPid";
    private enum NestedElementType { CHILDS, BLOCKS }

    public Element createProcessElement(Document doc, Process process) {
        Element procEl = doc.createElement(PROCESS);
        createElement(procEl, doc.createElement(PID), String.valueOf(process.getPid()));
        createElement(procEl, doc.createElement(APPLICATIONNAME), process.getCaller().getApplicationName());
        createElement(procEl, doc.createElement(DATNAME), process.getCaller().getDatname());
        createElement(procEl, doc.createElement(USENAME), process.getCaller().getUsername());
        createElement(procEl, doc.createElement(CLIENT), process.getCaller().getClient());
        createElement(procEl, doc.createElement(BACKENDSTART), process.getQuery().getBackendStart());
        createElement(procEl, doc.createElement(QUERYSTART), process.getQuery().getQueryStart());
        createElement(procEl, doc.createElement(XACTSTART), process.getQuery().getExactStart());
        createElement(procEl, doc.createElement(STATE), process.getState());
        createElement(procEl, doc.createElement(STATECHANGE), process.getStateChange());

        Element blocks = doc.createElement(BLOCKED);
        for(Block block : process.getBlocks()) {
            blocks.appendChild(createBlockElement(doc, block));
        }
        procEl.appendChild(blocks);

        createElement(procEl, doc.createElement(QUERY), process.getQuery().getQueryString());
        createElement(procEl, doc.createElement(SLOWQUERY), String.valueOf(process.getQuery().isSlowQuery()));
        
        Element children = doc.createElement(CHILDREN);
        for(Process childProcess : process.getChildren()) {
            children.appendChild(createProcessElement(doc, childProcess));
        }
        procEl.appendChild(children);
        return procEl;
    }
    
    public Process parseProcess(Element el) {
        Query query = new Query(getNodeValue(el, QUERY),
                getNodeValue(el, BACKENDSTART),
                getNodeValue(el, QUERYSTART),
                getNodeValue(el, XACTSTART),
                Boolean.parseBoolean(getNodeValue(el, SLOWQUERY)));
        QueryCaller caller = new QueryCaller(getNodeValue(el, APPLICATIONNAME),
                getNodeValue(el, DATNAME),
                getNodeValue(el, USENAME),
                getNodeValue(el, CLIENT));
        Process process = new Process(Integer.parseInt(getNodeValue(el, PID)),
                caller,
                query,
                getNodeValue(el, STATE),
                getNodeValue(el, STATECHANGE));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();

        // parse blocks
        parseChildrenAndBlocks(el, process, xp, NestedElementType.BLOCKS);

        // parse children
        parseChildrenAndBlocks(el, process, xp, NestedElementType.CHILDS);

        return process;
    }

    private void createElement(Element procEl, Element rows, String textContent){
        rows.setTextContent(textContent);
        procEl.appendChild(rows);
    }
    
    private String getNodeValue(Element el, String nodeName) {
        Node node = el.getElementsByTagName(nodeName).item(0).getFirstChild();
        if(node == null) {
            return "";
        }
        return node.getNodeValue();
    }

    private Element createBlockElement(Document doc, Block block) {
        Element procEl = doc.createElement(BLOCK);
        createElement(procEl, doc.createElement(BLOCKING_PID), String.valueOf(block.getBlockingPid()));
        createElement(procEl, doc.createElement(RELATION), block.getRelation());
        createElement(procEl, doc.createElement(LOCKTYPE), block.getLocktype());

        return procEl;
    }

    private void parseChildrenAndBlocks(Element el, Process process, XPath xp, NestedElementType type) {
        NodeList nodeList = null;
        String expression;
        switch (type) {
            case BLOCKS:
                expression = "blocked/block";
                break;
            case CHILDS:
                expression = "children/process";
                break;
            default:
                LOG.error("Unexpected NestedElementType: " + type);
                return;
        }

        try {
            nodeList = (NodeList)xp.evaluate(expression, el, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            LOG.error("Ошибка XPathExpressionException: " + e.getMessage());
        }
        if (nodeList != null) {
            for(int i = 0; i<nodeList.getLength(); i++) {
                Node processNode = nodeList.item(i);
                if (processNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element procEl = (Element)processNode;
                switch (type) {
                    case BLOCKS:
                        process.addBlock(
                                new Block(Integer.parseInt(getNodeValue(procEl, BLOCKING_PID)),
                                        getNodeValue(procEl, LOCKTYPE),
                                        getNodeValue(procEl, RELATION)));
                        break;
                    case CHILDS:
                        Process proc = parseProcess(procEl);
                        proc.setParents(process);
                        process.addChildren(proc);
                        break;
                    default:
                        LOG.error("Unexpected NestedElementType: " + type);
                        return;
                }
            }
        }
    }
}
