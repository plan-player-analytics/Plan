package com.djrapitops.plan.datahandlers.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.datahandlers.DataHandler;
import com.djrapitops.plan.datahandlers.LocationHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlanPlayerMoveListener implements Listener {

    private final Plan plugin;
    private final DataHandler handler;
    private final LocationHandler locationH;

    public PlanPlayerMoveListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        locationH = handler.getLocationHandler();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getWorld() == to.getWorld()) {
            return;
        }
        locationH.addLocation(p.getUniqueId(), to);
    }
}
