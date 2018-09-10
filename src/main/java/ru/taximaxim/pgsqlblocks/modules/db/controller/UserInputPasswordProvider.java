package ru.taximaxim.pgsqlblocks.modules.db.controller;

public interface UserInputPasswordProvider {
    String getPasswordFromUser(DBController controller); // TODO не пытаться подключиться если пользователь нажал Cancel
}