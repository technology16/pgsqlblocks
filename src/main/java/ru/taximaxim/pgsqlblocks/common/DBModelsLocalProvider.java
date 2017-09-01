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
}
