
package com.djrapitops.plan.api;

import com.djrapitops.plan.Plan;
import java.util.HashMap;

public interface Hook {

    public HashMap<String, DataPoint> getData(String player) throws Exception;
    public HashMap<String, DataPoint> getAllData(String player) throws Exception;
    
    public default void setPlan(Plan plan) throws Exception {
        
    }

}
