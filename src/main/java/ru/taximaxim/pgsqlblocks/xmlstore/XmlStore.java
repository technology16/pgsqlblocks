/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 - 2022 "Technology" LLC
 * %
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
package ru.taximaxim.pgsqlblocks.xmlstore;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class XmlStore<T> {

    private static final Logger LOG = LogManager.getLogger(XmlStore.class);

    protected final String rootTag;

    protected XmlStore(String rootTag) {
        this.rootTag = rootTag;
    }

    protected Element createSubElement(Document xml, Element parent, String name, String value) {
        Element newElement = xml.createElement(name);
        newElement.setTextContent(value);
        parent.appendChild(newElement);
        return newElement;
    }

    protected abstract Path getXmlFile();

    public List<T> readObjects() {
        try (Reader xmlReader = Files.newBufferedReader(getXmlFile(), StandardCharsets.UTF_8)) {
            return getObjects(readXml(xmlReader));
        } catch (NoSuchFileException ex) {
            return new ArrayList<>();
        } catch (IOException | SAXException ex) {
            LOG.error(ex);
            return new ArrayList<>();
        }
    }

    protected List<T> getObjects(Document xml) {
        List<T> objects = new ArrayList<>();
        Element root = (Element) xml.getElementsByTagName(rootTag).item(0);
        NodeList nList = root.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                objects.add(parseElement(node));
            }
        }
        return objects;
    }

    protected abstract T parseElement(Node node);

    public void writeObjects(List<T> list) {
        try {
            Path path = getXmlFile();
            Files.createDirectories(path.getParent());
            try (Writer xmlWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element root = xml.createElement(rootTag);
                xml.appendChild(root);
                appendChildren(xml, root, list);
                serializeXml(xml, xmlWriter);
            }

        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            LOG.error(ex);
        }
    }

    protected abstract void appendChildren(Document xml, Element root, List<T> list);

    /**
     * Reads (well-formed) list XML and checks it for basic validity:
     * root node must be <code>&lt;rootTagName&gt;</code>
     */
    private Document readXml(Reader reader) throws IOException, SAXException {
        try {
            Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(reader));
            xml.normalize();

            if (!xml.getDocumentElement().getNodeName().equals(rootTag)) {
                throw new IOException("XML root element name is not as requested");
            }

            return xml;
        } catch (ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    private void serializeXml(Document xml, Writer writer) throws TransformerException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty(OutputKeys.METHOD, "xml");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        tf.transform(new DOMSource(xml), new StreamResult(writer));
    }
}