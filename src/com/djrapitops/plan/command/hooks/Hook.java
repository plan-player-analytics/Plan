
package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import java.util.HashMap;

public interface Hook {

    public HashMap<String, String> getData(String player) throws Exception;
    public HashMap<String, String> getAllData(String player) throws Exception;
    
    public default void setPlan(Plan plan) throws Exception {
        
    }

}
