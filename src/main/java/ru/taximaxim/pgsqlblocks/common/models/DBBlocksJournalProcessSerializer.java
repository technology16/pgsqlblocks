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
package ru.taximaxim.pgsqlblocks.common.models;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;

import java.util.Date;

public class DBBlocksJournalProcessSerializer {

    public static final String JOURNAL_PROCESS_ROOT_ELEMENT_TAG_NAME = "journalProcess";

    private static final String CREATE_DATE_ELEMENT_TAG_NAME = "createDate";

    private static final String CLOSE_DATE_ELEMENT_TAG_NAME = "closeDate";

    private static DBProcessSerializer processSerializer = new DBProcessSerializer();
    private final DateUtils dateUtils = new DateUtils();

    public Element serialize(Document document, DBBlocksJournalProcess journalProcess) {

        DBProcessSerializer processSerializer = new DBProcessSerializer();

        Element rootElement = document.createElement(JOURNAL_PROCESS_ROOT_ELEMENT_TAG_NAME);

        Element createDateElement = document.createElement(CREATE_DATE_ELEMENT_TAG_NAME);
        createDateElement.setTextContent(dateUtils.dateToStringWithTz(journalProcess.getCreateDate()));
        rootElement.appendChild(createDateElement);

        Element closeDateElement = document.createElement(CLOSE_DATE_ELEMENT_TAG_NAME);
        closeDateElement.setTextContent(dateUtils.dateToStringWithTz(journalProcess.getCloseDate()));
        rootElement.appendChild(closeDateElement);

        Element processElement = processSerializer.serialize(document, journalProcess.getProcess());
        rootElement.appendChild(processElement);
        return rootElement;
    }

    public DBBlocksJournalProcess deserialize(Element xmlElement) {
        String createDateString = xmlElement.getElementsByTagName(CREATE_DATE_ELEMENT_TAG_NAME).item(0).getTextContent();
        String closeDateString = xmlElement.getElementsByTagName(CLOSE_DATE_ELEMENT_TAG_NAME).item(0).getTextContent();
        DBProcess process = processSerializer.deserialize(xmlElement, false);
        Date createDate = dateUtils.dateFromString(createDateString);
        Date closeDate = dateUtils.dateFromString(closeDateString);
        return new DBBlocksJournalProcess(createDate, closeDate, process);
    }
}
