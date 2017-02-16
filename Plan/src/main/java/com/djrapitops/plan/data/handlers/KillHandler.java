package main.java.com.djrapitops.plan.data.handlers;

import java.util.Date;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.UserData;

/**
 *
 * @author Rsl1122
 */
public class KillHandler {
    private Plan plugin;

    public KillHandler(Plan plugin) {
        this.plugin = plugin;
    }
    
    public void handlePlayerKill(UserData killerData, UUID victimUUID, String weapon) {
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
