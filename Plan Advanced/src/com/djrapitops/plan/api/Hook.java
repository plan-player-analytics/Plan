package com.djrapitops.plan.api;

import java.util.HashMap;

@Deprecated
public interface Hook {

    @Deprecated
    public HashMap<String, DataPoint> getData(String player) throws Exception;

    @Deprecated
    public HashMap<String, DataPoint> getAllData(String player) throws Exception;
}
