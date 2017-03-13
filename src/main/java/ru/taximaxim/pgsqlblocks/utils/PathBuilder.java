package ru.taximaxim.pgsqlblocks.utils;

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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class PathBuilder {
    
    private static final Logger LOG = Logger.getLogger(PathBuilder.class);
    
    private static PathBuilder instance;
    
    private Path path;
    
    private PathBuilder() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                path = Paths.get(System.getProperty("user.home"), "pgSqlBlocks");
                Files.createDirectories(path);
                Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
            } else {
                path = Paths.get(System.getProperty("user.home"), ".pgSqlBlocks");
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            LOG.error(String.format("Ошибка создания директории %s: %s", path, e.getMessage()));
        }
    }
    
    public static PathBuilder getInstance() {
        if(instance == null) {
            instance = new PathBuilder();
        }
        return instance;
    }
    
    public Path getBlockHistoryDir() {
        Path blocksHistoryDir = Paths.get(path.toString(), "blocksHistory");
        if (Files.notExists(blocksHistoryDir)) {
            try {
                Files.createDirectory(blocksHistoryDir);
            } catch (IOException e) {
                LOG.error(String.format("Ошибка создания директории %s: %s", blocksHistoryDir, e.getMessage()));
            }
        }
        return blocksHistoryDir;
    }
    
    public Path getBlockHistoryPath() {
        Path blocksHistoryDir = getBlockHistoryDir();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        String dateTime = sdf.format(new Date(System.currentTimeMillis()));
        
        return Paths.get(blocksHistoryDir.toString(), String.format("blocksHistory-%s.xml", dateTime));
    }
    
    public Path getServersPath() {
        Path serversPath = Paths.get(path.toString(), "servers.xml");
        if (Files.notExists(serversPath)) {
            try {
                Files.createFile(serversPath);
            } catch (IOException e) {
                LOG.error(String.format("Ошибка создания файла %s: %s", serversPath, e.getMessage()));
            }
        }
        return serversPath;
    }
    // TODO разные конфиги log4j.propertires для разных ОС 
    public Path getPropertiesPath() {
        Path propPath = Paths.get(path.toString(), "pgsqlblocks.properties");
        if (Files.notExists(propPath)) {
            try {
                Files.createFile(propPath);
            } catch (IOException e) {
                LOG.error(String.format("Ошибка создания файла %s: %s", propPath, e.getMessage()));
            }
        }
        return propPath;
    }
}
