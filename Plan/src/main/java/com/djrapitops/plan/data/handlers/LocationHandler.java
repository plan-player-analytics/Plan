package main.java.com.djrapitops.plan.data.handlers;

import java.util.Collection;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import org.bukkit.Location;

/**
 *
 * @author Rsl1122
 */
public class LocationHandler {

    private Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public LocationHandler(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds location to the UserData if it is not being saved.
     *
     * @param data UserData of player
     * @param loc Location from the MoveEvent listener.
     */
    public void addLocation(UserData data, Location loc) {
        if (!data.isAccessed()) {
            data.addLocation(loc);
        } else {
            // TODO: Location scheduler
        }
    }

    /**
     * Adds multiple locaitons to the UserData.
     *
     * @param data Userdata of Player
     * @param locs The Locations that are added.
     */
    public void addLocations(UserData data, Collection<Location> locs) {
        if (!data.isAccessed()) {
            data.addLocations(locs);
        }
    }
}
