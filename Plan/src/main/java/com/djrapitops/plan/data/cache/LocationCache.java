package main.java.com.djrapitops.plan.data.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

/**
 * This class is used to save locations players walk at.
 *
 * Extends SessionCache so that DataCacheHandler can have all 3 classes methods.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class LocationCache extends SessionCache {

    private HashMap<UUID, List<Location>> locations;

    /**
     * Class Constructor.
     */
    public LocationCache() {
        super();
        locations = new HashMap<>();
    }

    /**
     * Add a location for a player to the list.
     *
     * @param uuid UUID of the player.
     * @param loc Location the player moved to.
     */
    public void addLocation(UUID uuid, Location loc) {
        if (!locations.containsKey(uuid)) {
            locations.put(uuid, new ArrayList<>());
        }
        locations.get(uuid).add(loc);
    }

    /**
     * Add multiple locations to the list.
     *
     * @param uuid UUID of the player.
     * @param locs Locations the player moved to.
     */
    public void addLocations(UUID uuid, Collection<Location> locs) {
        if (!locations.containsKey(uuid)) {
            locations.put(uuid, new ArrayList<>());
        }
        locations.get(uuid).addAll(locs);
    }

    /**
     * Get the list of locations in the cache for saving the UserData object to
     * Database.
     *
     * @param uuid UUID of the player.
     * @return List of locations the player has been at.
     */
    public List<Location> getLocationsForSaving(UUID uuid) {
        if (!locations.containsKey(uuid)) {
            return new ArrayList<>();
        }
        return locations.get(uuid);
    }

    /**
     * Used to clear the locations from the locationcache.
     *
     * @param uuid UUID of the player.
     */
    public void clearLocations(UUID uuid) {
        locations.remove(uuid);
    }
}
