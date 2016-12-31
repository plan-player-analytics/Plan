package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.UserData;
import java.util.Date;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BasicInfoHandler {

    private DataHandler handler;

    public BasicInfoHandler(Plan plugin, DataHandler h) {
        this.handler = h;
    }

    public void save(Player player) {

    }

    public void handleLogIn(PlayerLoginEvent event, UserData data) {
        Player player = event.getPlayer();
        data.addNickname(player.getDisplayName());
        data.addIpAddress(event.getAddress());
    }

    void handleReload(Player player, UserData data) {
        data.addNickname(player.getDisplayName());
        data.addIpAddress(player.getAddress().getAddress());
    }
}
