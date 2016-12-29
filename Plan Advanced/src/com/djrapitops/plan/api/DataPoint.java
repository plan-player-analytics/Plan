
package com.djrapitops.plan.api;

public class DataPoint {
    private String data;
    private final DataType type;

    public DataPoint(String data, DataType type) {
        this.data = data;
        this.type = type;
    }

    public String data() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public DataType type() {
        return type;
    }
    
    
}
