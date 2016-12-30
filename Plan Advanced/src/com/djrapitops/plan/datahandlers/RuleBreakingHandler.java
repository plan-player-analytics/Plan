package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.UserData;
import java.util.Date;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RuleBreakingHandler {

    private final DataHandler handler;

    public RuleBreakingHandler(Plan plugin, DataHandler h) {
        this.handler = h;
    }   

    public void handleLogout(PlayerQuitEvent event, UserData data) {
        Player player = event.getPlayer();
        data.updateBanned(player);
    }

    public void handleKick(PlayerKickEvent event, UserData data) {
        Player player = event.getPlayer();
        data.setTimesKicked(data.getTimesKicked()+1);
        data.setPlayTime(data.getPlayTime()+(data.getLastPlayed()-new Date().getTime()));
        data.setLastPlayed(player.getLastPlayed());
    }
}
