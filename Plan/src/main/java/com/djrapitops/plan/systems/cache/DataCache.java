package main.java.com.djrapitops.plan.systems.cache;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;

/**
 * This Class contains the Cache.
 * <p>
 * It is used to store command use, active sessions and Unsaved TPS objects
 * objects in memory.
 * <p>
 * Its methods can be used to access all the data it stores and to clear them.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class DataCache extends SessionCache {

    private final Database db;

    /**
     * Class Constructor.
     * <p>
     * Gets the Database from the plugin. Starts the queues. Registers
     * Asynchronous Periodic Save Task
     *
     * @param plugin Current instance of Plan
     */
    public DataCache(Plan plugin) {
        super(plugin); // Initializes Session & Location cache.
        db = plugin.getDB();
    }

}
