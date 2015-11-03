package ru.taximaxim.pgsqlblocks;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class XmlDocumentWorker {
    
    private static final Logger LOG = Logger.getLogger(XmlDocumentWorker.class);
    private static final String SERVERS = "servers";
    
    private File xmlFile;
    
    public XmlDocumentWorker(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void save(Document doc, File file) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            try {
                transformer.transform(source, result);
            } catch (TransformerException e) {
                LOG.error(e);
            }
        } catch (TransformerConfigurationException e) {
            LOG.error(e);
        }
    }
    
    public void save(Document doc) {
        save(doc, xmlFile);
    }
    
    public Document open() {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder db = df.newDocumentBuilder();
            try {
                doc = db.parse(xmlFile);
            } catch (SAXException | IOException e) {
                LOG.error("Не найден файл конфигурации");
                createConfFile();
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Ошибка ParserConfigurationException: " + e.getMessage());
        }
        
        return doc;
    }
    
    private void createConfFile() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(SERVERS);
            doc.appendChild(rootElement);
            save(doc);
        } catch (ParserConfigurationException e) {
            LOG.error(e);
        }
        LOG.info("Создан файл конфигурации " + xmlFile.toString());
    }
}