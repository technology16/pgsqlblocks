package ru.taximaxim.treeviewer.utils;


import java.util.ResourceBundle;

public enum Images {

    UPDATE,
    FILTER,
    TABLE,
    DEFAULT;

    Images() {
    }

    public String getImageAddr() {
        switch(this) {
            case UPDATE:
                return "images/update_16.png";
            case FILTER:
                return "images/filter.png";
            case TABLE:
                return "images/table_16.png";
            default:
                return "images/void_16.png";
        }
    }

    public String getDescription(ResourceBundle resources) { // TODO: 29.08.18 Inner bundle
        switch(this) {
            case UPDATE:
                return resources.getString("update");
            case FILTER:
                return resources.getString("filter");
            case TABLE:
                return resources.getString("columns");
            default:
                return resources.getString("default_action");
        }
    }
}
