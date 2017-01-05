package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.UserData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public class BasicInfoHandler {

    private DataHandler handler;

    public BasicInfoHandler(Plan plugin, DataHandler h) {
        this.handler = h;
    }

    public void handleLogIn(PlayerJoinEvent event, UserData data) {
        Player player = event.getPlayer();
        data.addNickname(player.getDisplayName());
        data.addIpAddress(player.getAddress().getAddress());
    }

    void handleReload(Player player, UserData data) {
        data.addNickname(player.getDisplayName());
        data.addIpAddress(player.getAddress().getAddress());
    }
}
