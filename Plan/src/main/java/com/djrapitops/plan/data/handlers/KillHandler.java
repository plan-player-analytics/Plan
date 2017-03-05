package main.java.com.djrapitops.plan.data.handlers;

import java.sql.SQLException;
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

    /**
     *
     * @param plugin
     */
    public KillHandler(Plan plugin) {
        this.plugin = plugin;
    }
    
    /**
     *
     * @param killerData
     * @param victimUUID
     * @param weapon
     */
    public void handlePlayerKill(UserData killerData, UUID victimUUID, String weapon) {
        long now = new Date().toInstant().getEpochSecond()*(long)1000;
        int victimID;
        try {
            victimID = plugin.getDB().getUserId(victimUUID+"");
        } catch (SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
            return;
        }
        killerData.addPlayerKill(new KillData(victimUUID, victimID, weapon, now));
    }
    
    /**
     *
     * @param data
     */
    public void handlePlayerDeath(UserData data) {
        data.setDeaths(data.getDeaths()+1);
    }
    
    /**
     *
     * @param data
     */
    public void handleMobKill(UserData data) {
        data.setMobKills(data.getMobKills()+1);
    }
    
}
