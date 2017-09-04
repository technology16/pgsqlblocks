package ru.taximaxim.pgsqlblocks.common.models;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class DBModelsListSerializer {

    private static final String ROOT_ELEMENT_TAG_NAME = "servers";
    private static final String ELEMENTS_ROOT_ELEMENT_TAG_NAME = "server";

    public List<DBModel> deserialize(Document document) {
        List<DBModel> dbModels = new ArrayList<>();
        DBModelSerializer modelSerializer = new DBModelSerializer();
        NodeList nodeList = document.getElementsByTagName(ELEMENTS_ROOT_ELEMENT_TAG_NAME);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            DBModel model = modelSerializer.deserialize((Element) node);
            dbModels.add(model);
        }
        return dbModels;
    }

    public Document serialize(Document document, List<DBModel> dbModelList) {
        Element rootElement = document.createElement(ROOT_ELEMENT_TAG_NAME);
        DBModelSerializer modelSerializer = new DBModelSerializer();
        for (DBModel dbModel : dbModelList) {
            Element dbModelElement = modelSerializer.serialize(document, dbModel);
            rootElement.appendChild(dbModelElement);
        }
        document.appendChild(rootElement);
        return document;
    }
}
