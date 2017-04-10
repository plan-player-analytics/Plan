/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Risto
 */
public class LogoutHandling {
    public static void processLogoutInfo(UserData data, long time, boolean banned) {
        data.setPlayTime(data.getPlayTime() + (time - data.getLastPlayed()));
        data.setLastPlayed(time);        
        data.updateBanned(banned);
        getPlugin(Plan.class).getHandler().getSessionHandler().endSession(data);
    }
}
