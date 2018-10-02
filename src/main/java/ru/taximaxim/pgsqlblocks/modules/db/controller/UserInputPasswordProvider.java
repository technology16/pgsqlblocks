package ru.taximaxim.pgsqlblocks.modules.db.controller;

import ru.taximaxim.pgsqlblocks.utils.UserCancelException;

public interface UserInputPasswordProvider {

    String getPasswordFromUser(DBController controller) throws UserCancelException;
}