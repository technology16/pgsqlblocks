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
            LOG.error("Ошибка создания директории pgSqlBlocks: " + e.getMessage());
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
                LOG.error("Ошибка создания директории pgSqlBlocks/blocksHistory: " + e.getMessage());
            }
        }
        return blocksHistoryDir;
    }
    
    public Path getBlockHistoryPath() {
        Path blocksHistoryDir = getBlockHistoryDir();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        Date time = new Date(System.currentTimeMillis());
        String dateTime = sdf.format(time);
        
        return Paths.get(blocksHistoryDir.toString(), String.format("%s-%s.xml", "blocksHistory", dateTime));
    }
    
    public Path getServersPath() {
        Path serversPath = Paths.get(path.toString(), "servers.xml");
        if (Files.notExists(serversPath)) {
            try {
                Files.createFile(serversPath);
            } catch (IOException e) {
                LOG.error("Ошибка создания файла pgSqlBlocks/servers.xml: " + e.getMessage());
            }
        }
        return serversPath;
    }
}
