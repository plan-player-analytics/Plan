package main.java.com.djrapitops.plan.data.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.Location;

/**
 *
 * @author Rsl1122
 */
public class LocationHandler {

    private Plan plugin;
    private HashMap<UUID, List<Location>> locations;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public LocationHandler(Plan plugin) {
        this.plugin = plugin;
        locations = new HashMap<>();
    }

    /**
     * Adds location to the UserData if it is not being saved.
     *
     * @param uuid UUID of player
     * @param loc Location from the MoveEvent listener.
     */
    public void addLocation(UUID uuid, Location loc) {
        if (!locations.containsKey(uuid)) {
            locations.put(uuid, new ArrayList<>());
        }
        locations.get(uuid).add(loc);
    }

    /**
     * Adds multiple locaitons to the UserData.
     *
     * @param uuid UUID of player
     * @param locs The Locations that are added.
     */
    public void addLocations(UUID uuid, Collection<Location> locs) {
        if (!locations.containsKey(uuid)) {
            locations.put(uuid, new ArrayList<>());
        }
        locations.get(uuid).addAll(locs);
    }
    
    public List<Location> getLocationsForSaving(UUID uuid) {
        if (!locations.containsKey(uuid)) {
            return new ArrayList<>();
        }
        return locations.get(uuid);
    }
    
    public void clearLocations(UUID uuid) {
        locations.remove(uuid);
    }
}
