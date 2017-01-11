package com.djrapitops.plan.api;

/**
 * Old API part.
 *
 * @author Rsl1122
 * @deprecated Moved to PlanLite plugin
 */
@Deprecated
public class DataPoint {

    private String data;
    private final DataType type;

    /**
     *
     * @param data
     * @param type
     * @deprecated Moved to PlanLite plugin
     */
    @Deprecated
    public DataPoint(String data, DataType type) {
        this.data = data;
        this.type = type;
    }

    /**
     *
     * @return @deprecated Moved to PlanLite plugin
     */
    @Deprecated
    public String data() {
        return data;
    }

    /**
     *
     * @param data
     * @deprecated Moved to PlanLite plugin
     */
    @Deprecated
    public void setData(String data) {
        this.data = data;
    }

    /**
     *
     * @return @deprecated Moved to PlanLite plugin
     */
    @Deprecated
    public DataType type() {
        return type;
    }

}
