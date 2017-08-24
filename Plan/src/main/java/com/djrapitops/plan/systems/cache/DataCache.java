package main.java.com.djrapitops.plan.systems.cache;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;

import java.util.*;

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

    private final Map<UUID, String> playerNames;
    private final Map<UUID, String> displayNames;

    private final Set<UUID> playersWithFirstSession;

    /**
     * Class Constructor.
     * <p>
     * Gets the Database from the plugin. Starts the queues. Registers
     * Asynchronous Periodic Save Task
     *
     * @param plugin Current instance of Plan
     */
    public DataCache(Plan plugin) {
        super(plugin);
        db = plugin.getDB();

        playerNames = new HashMap<>();
        displayNames = new HashMap<>();
        playersWithFirstSession = new HashSet<>();
    }

    public void updateNames(UUID uuid, String playerName, String displayName) {
        playerNames.put(uuid, playerName);
        displayNames.put(uuid, displayName);
    }

    public String getName(UUID uuid) {
        return playerNames.get(uuid);
    }

    public String getDisplayName(UUID uuid) {
        return displayNames.get(uuid);
    }

    public void addFirstLeaveCheck(UUID uuid) {
        playersWithFirstSession.add(uuid);
    }

    public boolean isFirstSession(UUID uuid) {
        return playersWithFirstSession.contains(uuid);
    }

    public void clearFromFirstLeaveCheck(UUID uuid) {
        playersWithFirstSession.remove(uuid);
    }
}
