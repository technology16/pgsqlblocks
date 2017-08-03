/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * *
 * Copyright (C) 2017 "Technology" LLC
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package ru.taximaxim.pgsqlblocks.utils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;


public class XmlDocumentWorker {
    
    private static final Logger LOG = Logger.getLogger(XmlDocumentWorker.class);
    private static final String SERVERS = "servers";

    public void save(Document doc, File xmlFile) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            try {
                transformer.transform(source, result);
            } catch (TransformerException e) {
                LOG.error(e);
            }
        } catch (TransformerConfigurationException e) {
            LOG.error(e);
        }
    }
    
    public Document open(File xmlFile) {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder db = df.newDocumentBuilder();
            try {
                doc = db.parse(xmlFile);
            } catch (SAXException | IOException e) {
                LOG.info("Не найден файл конфигураци servers.xml", e);
                createConfFile(xmlFile);
            }
        } catch (ParserConfigurationException e) {
            LOG.error(String.format("Ошибка парсинга файла %s!", xmlFile), e);
        }
        
        return doc;
    }

    public Document openJournalFile(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = df.newDocumentBuilder();
        return db.parse(file);
    }
    
    private void createConfFile(File xmlFile) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(SERVERS);
            doc.appendChild(rootElement);
            save(doc, xmlFile);
        } catch (ParserConfigurationException e) {
            LOG.error(e);
        }
        LOG.info(String.format("Создан файл %s...", xmlFile));
    }
}