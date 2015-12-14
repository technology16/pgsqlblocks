package ru.taximaxim.pgsqlblocks.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public final class PathBuilder {
    
    private static final Logger LOG = Logger.getLogger(PathBuilder.class);
    
    private static PathBuilder instance;
    
    private Path path;
    
    private PathBuilder() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("nux") || os.contains("nix")) {
                path = Paths.get(System.getProperty("user.home"), ".pgSqlBlocks");
                Files.createDirectories(path);
            } else if (os.contains("win")) {
                path = Paths.get(System.getProperty("user.home"), "pgSqlBlocks");
                Files.createDirectories(path);
                Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
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
    
    public Path getLogsPath() {
        Path logsDir = Paths.get(path.toString(), "logs");
        if (Files.notExists(logsDir)) {
            try {
                Files.createDirectory(logsDir);
            } catch (IOException e) {
                LOG.error(String.format("Ошибка создания директории %s: %s", logsDir, e.getMessage()));
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date(System.currentTimeMillis()));
        Path logsFile = Paths.get(logsDir.toString(), String.format("log-%s.log", date));
        if (Files.notExists(logsFile)) {
            try {
                Files.createFile(logsFile);
            } catch (IOException e) {
                LOG.error(String.format("Ошибка создания файла %s: %s", logsFile, e.getMessage()));
            }
        }
        return logsFile;
    }
    
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
