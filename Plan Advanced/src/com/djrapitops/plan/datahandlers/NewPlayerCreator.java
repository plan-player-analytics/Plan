package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.DemographicsData;
import com.djrapitops.plan.database.UserData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class NewPlayerCreator {

    private Plan plugin;
    private Database db;
    private DataHandler handler;

    public NewPlayerCreator(Plan plugin, DataHandler h) {
        this.plugin = plugin;
        db = plugin.getDB();
        handler = h;
    }

    public void createNewPlayer(Player p) {
        UserData data = new UserData(p, new DemographicsData(), db);
        if (p.getGameMode() == null) {
            GameMode defaultGM = Bukkit.getServer().getDefaultGameMode();
            if (defaultGM != null) {
                data.setLastGamemode(defaultGM);
            } else {
                data.setLastGamemode(GameMode.SURVIVAL);
            }
        } else {
            data.setLastGamemode(p.getGameMode());
        }
        long zero = Long.parseLong("0");
        data.setPlayTime(zero);
        data.setTimesKicked(0);
        data.setLoginTimes(0);
        data.setLastGmSwapTime(zero);
        db.saveUserData(p.getUniqueId(), data);
    }

}
