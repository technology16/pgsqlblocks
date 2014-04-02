package ru.taximaxim.pgsqlblocks;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BlocksHistory {

    private static BlocksHistory bh;
    private static Logger log = Logger.getLogger(BlocksHistory.class);
    private ConcurrentHashMap<DbcData, List<Process>> hm = new ConcurrentHashMap<DbcData, List<Process>>();
    private static final String filePath = "BlocksHistory";
    private static final String fileName = "/blocksHistory";
    

    public static BlocksHistory getInstance() {
        if(bh == null)
            bh = new BlocksHistory();
        return bh;
    }

    private BlocksHistory() {
        File dir = new File(filePath);
        if(!dir.isDirectory())
            dir.mkdir();
    }

    public void add(DbcData dbc, Process process) {
        List<Process> list = hm.get(dbc);
        if(list == null) {
            list = new ArrayList<Process>();
            hm.put(dbc, list);
        }
        if(list.contains(process))
            return;
        list.add(process);
    }
    
    public synchronized void save() {
        if(hm.size() == 0){
            log.error("Не найдено блокировок для сохранения");
            return;
        }
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("servers");
            doc.appendChild(rootElement);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date time = new Date(System.currentTimeMillis());
            String dateTime = sdf.format(time);
            DbcDataList.getInstance().save(doc, String.format("%s%s-%s.xml", filePath,fileName,dateTime));
        } catch (ParserConfigurationException e) {
            log.error(e);
        }
        hm.clear();
    }
    
    public void open() {
        
    }
}
