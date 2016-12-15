package ru.taximaxim.pgsqlblocks.process;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BlockParser {
    private static final String CHILDREN = "children";
    
    private static final Logger LOG = Logger.getLogger(BlockParser.class);

    public Element createBlockElement(Document doc, Block block) {
        throw new NotImplementedException();
    }
}
