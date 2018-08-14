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
package ru.taximaxim.pgsqlblocks.common;

import org.w3c.dom.Document;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.common.models.DBModelsListSerializer;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class DBModelsLocalProvider implements DBModelsProvider {

    private XmlDocumentWorker documentWorker = new XmlDocumentWorker();
    private File file = PathBuilder.getInstance().getServersPath().toFile();

    private DBModelsListSerializer serializer = new DBModelsListSerializer();

    @Override
    public List<DBModel> get() {
        Document document = documentWorker.open(file);
        if (document == null) {
            return Collections.emptyList();
        }
        return serializer.deserialize(document);
    }

    @Override
    public void save(List<DBModel> models) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
            serializer.serialize(document, models);
            documentWorker.save(document, file);
        } catch (ParserConfigurationException e) {
            // FIXME
        }
    }

    @Override
    public boolean needUpdate() {
        Document document = documentWorker.open(file);
        return document != null && serializer.checkExistingNode(document);
    }

    @Override
    public void updateVersion() {
        //get list dbmodels
        //foreach get version and update
        //save list
    }
}
