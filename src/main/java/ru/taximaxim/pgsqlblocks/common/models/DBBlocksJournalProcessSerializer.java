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

    public Element serialize(Document document, DBBlocksJournalProcess journalProcess) {

        DBProcessSerializer processSerializer = new DBProcessSerializer();

        Element rootElement = document.createElement(JOURNAL_PROCESS_ROOT_ELEMENT_TAG_NAME);

        Element createDateElement = document.createElement(CREATE_DATE_ELEMENT_TAG_NAME);
        createDateElement.setTextContent(DateUtils.dateToStringWithTz(journalProcess.getCreateDate()));
        rootElement.appendChild(createDateElement);

        Element closeDateElement = document.createElement(CLOSE_DATE_ELEMENT_TAG_NAME);
        closeDateElement.setTextContent(DateUtils.dateToStringWithTz(journalProcess.getCloseDate()));
        rootElement.appendChild(closeDateElement);

        Element processElement = processSerializer.serialize(document, journalProcess.getProcess());
        rootElement.appendChild(processElement);
        return rootElement;
    }

    public DBBlocksJournalProcess deserialize(Element xmlElement) {
        String createDateString = xmlElement.getElementsByTagName(CREATE_DATE_ELEMENT_TAG_NAME).item(0).getTextContent();
        String closeDateString = xmlElement.getElementsByTagName(CLOSE_DATE_ELEMENT_TAG_NAME).item(0).getTextContent();
        DBProcess process = processSerializer.deserialize(xmlElement, false);
        Date createDate = DateUtils.dateFromString(createDateString);
        Date closeDate = DateUtils.dateFromString(closeDateString);
        return new DBBlocksJournalProcess(createDate, closeDate, process);
    }


}
