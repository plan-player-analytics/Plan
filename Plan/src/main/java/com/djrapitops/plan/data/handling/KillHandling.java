/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling;

import java.sql.SQLException;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.UserData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Risto
 */
public class KillHandling {

    public static void processKillInfo(UserData data, long time, LivingEntity dead, String weaponName) {
        Plan plugin = getPlugin(Plan.class);
        if (dead instanceof Player) {
            Player deadPlayer = (Player) dead;
            int victimID;
            try {
                UUID victimUUID = deadPlayer.getUniqueId();
                victimID = plugin.getDB().getUserId(victimUUID + "");
                if (victimID == -1) {
                    return;
                }
                data.addPlayerKill(new KillData(victimUUID, victimID, weaponName, time));
            } catch (SQLException e) {
                plugin.toLog("main.java.com.djrapitops.plan.KillHandling", e);                
            }
        } else {
            data.setMobKills(data.getMobKills() + 1);
        }
    }
}
