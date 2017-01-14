package com.djrapitops.plan.api;

import java.util.HashMap;

/**
 * Old API Part
 *
 * @author Rsl1122
 * @deprecated
 */
@Deprecated
public interface Hook {

    /**
     *
     * @param player
     * @return
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public HashMap<String, DataPoint> getData(String player) throws Exception;

    /**
     *
     * @param player
     * @return
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public HashMap<String, DataPoint> getAllData(String player) throws Exception;
}
