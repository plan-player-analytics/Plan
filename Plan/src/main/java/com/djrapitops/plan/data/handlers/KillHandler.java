
package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import java.util.Date;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.KillData;

/**
 *
 * @author Rsl1122
 */
public class KillHandler {
    private Plan plugin;

    public KillHandler(Plan plugin) {
        this.plugin = plugin;
    }
    
    public void handlePlayerKill(UserData killerData, UserData victim, String weapon) {
        UUID victimUUID = victim.getUuid();
        long now = new Date().toInstant().getEpochSecond()*(long)1000;
        int victimID = plugin.getDB().getUserId(victimUUID+"");
        killerData.addPlayerKill(new KillData(victimUUID, victimID, weapon, now));
    }
    
    public void handlePlayerDeath(UserData data) {
        data.setDeaths(data.getDeaths()+1);
    }
    
    public void handleMobKill(UserData data) {
        data.setMobKills(data.getMobKills()+1);
    }
    
}
