package ru.taximaxim.pgsqlblocks.utils;

public class FilterItem {
    
    private String key;
    
    private String operation;
    
    private String value;

    public FilterItem(String key, String operation, String value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    public void itemReset() {
        this.operation = "";
        this.value = "";
    }
}
