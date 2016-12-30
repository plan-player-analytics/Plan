package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.UserData;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

public class LocationHandler {

    private final DataHandler handler;

    public LocationHandler(Plan plugin, DataHandler h) {
        this.handler = h;
    }

    public void addLocation(UUID uuid, Location loc) {
        handler.getCurrentData(uuid).addLocation(loc);
    }

    public void addLocations(UUID uuid, Collection<Location> locs) {
        handler.getCurrentData(uuid).addLocations(locs);
    }

    public void handleLogOut(PlayerQuitEvent event, UserData data) {
        Player p = event.getPlayer();
        handler.getCurrentData(p.getUniqueId()).setBedLocation(p.getBedSpawnLocation());
    }
}
