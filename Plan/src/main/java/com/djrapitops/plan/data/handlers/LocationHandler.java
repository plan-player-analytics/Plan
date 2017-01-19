package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import java.util.Collection;
import java.util.UUID;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.player.PlayerQuitEvent;
import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public class LocationHandler {

    private final DataCacheHandler handler;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     * @param h Current instance of DataCacheHandler
     */
    public LocationHandler(Plan plugin, DataCacheHandler h) {
        this.handler = h;
    }

    /**
     * Adds location to the UserData if it is not being saved.
     *
     * @param uuid UUID of the matching Player
     * @param loc Location from the MoveEvent listener.
     */
    public void addLocation(UUID uuid, Location loc) {
        UserData data = handler.getCurrentData(uuid);
        if (!data.isAccessed()) {
            data.addLocation(loc);
        } else {
            // TODO: Location scheduler
        }
    }

    /**
     * Adds multiple locaitons to the UserData.
     *
     * @param uuid UUID of the matching Player
     * @param locs The Locations that are added.
     */
    public void addLocations(UUID uuid, Collection<Location> locs) {
        handler.getCurrentData(uuid).addLocations(locs);
    }

    /**
     * Handles QuitEvent by updating BedLocation.
     *
     * Uses OfflinePlayer to prevent null bedlocation.
     *
     * @param event QuitEvent from Listener.
     * @param data UserData matching Player.
     */
    public void handleLogOut(PlayerQuitEvent event, UserData data) {
        OfflinePlayer p = getOfflinePlayer(event.getPlayer().getUniqueId());
        Location bedSpawnLocation = p.getBedSpawnLocation();
        if (bedSpawnLocation == null) {
            return;
        }
        handler.getCurrentData(p.getUniqueId()).setBedLocation(bedSpawnLocation);
    }
}
