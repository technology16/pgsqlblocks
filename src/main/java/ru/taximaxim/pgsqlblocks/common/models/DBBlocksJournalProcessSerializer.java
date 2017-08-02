package ru.taximaxim.pgsqlblocks.common.models;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.taximaxim.pgsqlblocks.utils.DateUtils;


public class DBBlocksJournalProcessSerializer {

    private static final String JOURNAL_PROCESS_ROOT_ELEMENT_TAG_NAME = "journalProcess";

    private static final String CREATE_DATE_ELEMENT_TAG_NAME = "createDate";

    private static final String CLOSE_DATE_ELEMENT_TAG_NAME = "closeDate";

    public Element serialize(Document document, DBBlocksJournalProcess journalProcess) {

        DBProcessSerializer processSerializer = new DBProcessSerializer();

        Element rootElement = document.createElement(JOURNAL_PROCESS_ROOT_ELEMENT_TAG_NAME);

        Element createDateElement = document.createElement(CREATE_DATE_ELEMENT_TAG_NAME);
        createDateElement.setTextContent(DateUtils.dateToStringWithTz(journalProcess.getCreateDate()));

        Element closeDateElement = document.createElement(CLOSE_DATE_ELEMENT_TAG_NAME);
        closeDateElement.setTextContent(DateUtils.dateToStringWithTz(journalProcess.getCloseDate()));

        Element processElement = processSerializer.serialize(document, journalProcess.getProcess());
        rootElement.appendChild(processElement);
        return rootElement;
    }


}
