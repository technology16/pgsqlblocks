package ru.taximaxim.pgsqlblocks.common;

import org.w3c.dom.Document;
import ru.taximaxim.pgsqlblocks.common.models.DBModelsListSerializer;
import ru.taximaxim.pgsqlblocks.common.models.DBModel;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;
import ru.taximaxim.pgsqlblocks.utils.XmlDocumentWorker;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class DBModelsLocalProvider implements DBModelsProvider {

    private XmlDocumentWorker documentWorker = new XmlDocumentWorker();
    private File file = PathBuilder.getInstance().getServersPath().toFile();

    @Override
    public List<DBModel> get() {
        Document document = documentWorker.open(file);
        if (document == null) {
            return Collections.emptyList();
        }
        DBModelsListSerializer serializer = new DBModelsListSerializer();
        return serializer.deserialize(document);
    }

    @Override
    public void save(List<DBModel> models) {
        DBModelsListSerializer serializer = new DBModelsListSerializer();
        Document document = documentWorker.open(file);
        serializer.serialize(document, models);
        documentWorker.save(document, file);
    }


}
