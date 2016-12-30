package com.djrapitops.plan.api;

@Deprecated
public class DataPoint {

    private String data;
    private final DataType type;

    @Deprecated
    public DataPoint(String data, DataType type) {
        this.data = data;
        this.type = type;
    }

    @Deprecated
    public String data() {
        return data;
    }

    @Deprecated
    public void setData(String data) {
        this.data = data;
    }

    @Deprecated
    public DataType type() {
        return type;
    }

}
