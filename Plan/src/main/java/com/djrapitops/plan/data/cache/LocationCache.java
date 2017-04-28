package main.java.com.djrapitops.plan.data.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

/**
 *
 * @author Rsl1122
 */
public class LocationCache extends SessionCache{

    private HashMap<UUID, List<Location>> locations;

    /**
     * Class Constructor.
     *
     */
    public LocationCache() {
        super();
        locations = new HashMap<>();
    }

    /**
     *
     * @param uuid
     * @param loc
     */
    public void addLocation(UUID uuid, Location loc) {
        if (!locations.containsKey(uuid)) {
            locations.put(uuid, new ArrayList<>());
        }
        locations.get(uuid).add(loc);
    }

    /**
     *
     * @param uuid
     * @param locs
     */
    public void addLocations(UUID uuid, Collection<Location> locs) {
        if (!locations.containsKey(uuid)) {
            locations.put(uuid, new ArrayList<>());
        }
        locations.get(uuid).addAll(locs);
    }
    
    /**
     *
     * @param uuid
     * @return
     */
    public List<Location> getLocationsForSaving(UUID uuid) {
        if (!locations.containsKey(uuid)) {
            return new ArrayList<>();
        }
        return locations.get(uuid);
    }
   
    /**
     *
     * @param uuid
     */
    public void clearLocations(UUID uuid) {
        locations.remove(uuid);
    }
}
