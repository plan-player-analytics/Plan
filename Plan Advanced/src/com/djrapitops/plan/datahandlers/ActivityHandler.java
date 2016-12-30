package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.UserData;
import java.util.Date;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ActivityHandler {

    private final Plan plugin;
    private final DataHandler handler;

    public ActivityHandler(Plan plugin, DataHandler h) {
        this.plugin = plugin;
        this.handler = h;
    }

    public boolean isFirstTimeJoin(UUID uuid) {        
        return !handler.getDB().wasSeenBefore(uuid);
    }
    
    public void saveToCache(Player player, UserData data) {
        long timeNow = new Date().getTime();
        data.setPlayTime(data.getPlayTime()+(data.getLastPlayed()-timeNow));
        data.setLastPlayed(timeNow);
    }

    public void handleLogIn(PlayerLoginEvent event, UserData data) {
        data.setLastPlayed(new Date().getTime());
        data.updateBanned(event.getPlayer());
    }

    public void handleLogOut(PlayerQuitEvent event, UserData data) {
        Player player = event.getPlayer();
        data.setPlayTime(data.getPlayTime()+(data.getLastPlayed()-new Date().getTime()));
        data.setLastPlayed(player.getLastPlayed());
    }
}
