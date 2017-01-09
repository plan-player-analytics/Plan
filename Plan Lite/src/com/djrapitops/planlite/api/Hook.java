
package com.djrapitops.planlite.api;

import com.djrapitops.planlite.PlanLite;
import java.util.HashMap;

public interface Hook {

    public HashMap<String, DataPoint> getData(String player) throws Exception;
    public HashMap<String, DataPoint> getAllData(String player) throws Exception;
    
    public default void setPlan(PlanLite plan) throws Exception {
        
    }

}
