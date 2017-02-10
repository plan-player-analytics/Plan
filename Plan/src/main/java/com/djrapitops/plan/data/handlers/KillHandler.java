
package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;

/**
 *
 * @author Rsl1122
 */
public class KillHandler {
    private Plan plugin;

    public KillHandler(Plan plugin) {
        this.plugin = plugin;
    }
    
    public void handlePlayerKill(UserData data) {
        data.setPlayerKills(data.getPlayerKills()+1);
    }
    
    public void handlePlayerDeath(UserData data) {
        data.setDeaths(data.getDeaths()+1);
    }
    
    public void handleMobKill(UserData data) {
        data.setMobKills(data.getMobKills()+1);
    }
    
}
