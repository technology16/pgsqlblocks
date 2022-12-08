/*-
 * ========================LICENSE_START=================================
 * pgSqlBlocks
 * %
 * Copyright (C) 2017-2022 "Technology" LLC
 * %
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
package ru.taximaxim.pgsqlblocks.xmlstore;

import java.nio.file.Path;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.taximaxim.pgsqlblocks.utils.ColumnLayout;
import ru.taximaxim.pgsqlblocks.utils.Columns;
import ru.taximaxim.pgsqlblocks.utils.PathBuilder;

public class ColumnLayoutsXmlStore extends XmlStore<ColumnLayout> {

    private static final String ROOT_TAG = "columns";

    private static final String COLUMN = "column";
    private static final String COLUMN_ORDER = "column_order";
    private static final String COLUMN_NAME = "column_name";
    private static final String COLUMN_SIZE = "column_size";

    private final String fileName;

    public ColumnLayoutsXmlStore(String fileName) {
        super(ROOT_TAG);
        this.fileName = fileName;
    }

    @Override
    protected Path getXmlFile() {
        return PathBuilder.getInstance().getColumnsPath().resolve(fileName);
    }

    @Override
    public List<ColumnLayout> readObjects() {
        List<ColumnLayout> layouts = super.readObjects();
        layouts.sort((o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
        return layouts;
    }

    @Override
    protected ColumnLayout parseElement(Node node) {
        Element el = (Element) node;
        int order = Integer.parseInt(el.getElementsByTagName(COLUMN_ORDER).item(0).getTextContent());
        String name = el.getElementsByTagName(COLUMN_NAME).item(0).getTextContent();
        String value = el.getElementsByTagName(COLUMN_SIZE).item(0).getTextContent();
        Integer width = value.isEmpty() ? null : Integer.valueOf(value);

        return new ColumnLayout(order, Columns.valueOf(name), width);
    }

    @Override
    protected void appendChildren(Document xml, Element root, List<ColumnLayout> list) {
        for (ColumnLayout layout : list) {
            Element parent = xml.createElement(COLUMN);
            root.appendChild(parent);
            createSubElement(xml, parent, COLUMN_ORDER, Integer.toString(layout.getOrder()));
            createSubElement(xml, parent, COLUMN_NAME, layout.getColumn().name());
            Integer i = layout.getWidth();
            String width = i == null ? "" : String.valueOf(i);
            createSubElement(xml, parent, COLUMN_SIZE, width);
        }
    }
}
