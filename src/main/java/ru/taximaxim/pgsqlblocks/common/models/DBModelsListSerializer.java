/*
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017 "Technology" LLC
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
