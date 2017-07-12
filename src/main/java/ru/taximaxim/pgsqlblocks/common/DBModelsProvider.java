package ru.taximaxim.pgsqlblocks.common;

import ru.taximaxim.pgsqlblocks.common.models.DBModel;

import java.util.List;

public interface DBModelsProvider {

    List<DBModel> get();

    void save(List<DBModel> models);

}
