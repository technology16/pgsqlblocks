package ru.taximaxim.pgsqlblocks.utils;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import static java.nio.file.Files.*;

public final class PathBuilder {

    private static final Logger LOG = Logger.getLogger(PathBuilder.class);

    private static PathBuilder instance;

    private Path path;

    private PathBuilder() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                path = Paths.get(System.getProperty("user.home"), "pgSqlBlocks");
                createDirectories(path);
                setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
            } else {
                path = Paths.get(System.getProperty("user.home"), ".pgSqlBlocks");
                createDirectories(path);
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

    @SuppressWarnings("squid:S3725") // Cannot resolve method notExists()
    public Path getBlockHistoryDir() {
        Path blocksHistoryDir = Paths.get(path.toString(), "blocksHistory");
        if (notExists(blocksHistoryDir)) {
            try {
                createDirectory(blocksHistoryDir);
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

    @SuppressWarnings("squid:S3725") // Cannot resolve method notExists()
    public Path getServersPath() {
        Path serversPath = Paths.get(path.toString(), "servers.xml");
        if (notExists(serversPath)) {
            try {
                createFile(serversPath);
            } catch (IOException e) {
                LOG.error(String.format("Ошибка создания файла %s: %s", serversPath, e.getMessage()));
            }
        }
        return serversPath;
    }
    // TODO разные конфиги log4j.propertires для разных ОС
    @SuppressWarnings("squid:S3725") // Cannot resolve method notExists()
    public Path getPropertiesPath() {
        Path propPath = Paths.get(path.toString(), "pgsqlblocks.properties");
        if (notExists(propPath)) {
            try {
                createFile(propPath);
            } catch (IOException e) {
                LOG.error(String.format("Ошибка создания файла %s: %s", propPath, e.getMessage()));
            }
        }
        return propPath;
    }
}
