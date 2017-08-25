package main.java.com.djrapitops.plan.systems.cache;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private final Map<UUID, Integer> firstSessionInformation;

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
        firstSessionInformation = new HashMap<>();
    }

    public void updateNames(UUID uuid, String playerName, String displayName) {
        playerNames.put(uuid, playerName);
        displayNames.put(uuid, displayName);
    }

    public String getName(UUID uuid) {
        return playerNames.get(uuid);
    }

    public String getDisplayName(UUID uuid) {
        String cached = displayNames.get(uuid);
        if (cached == null) {
            List<String> nicknames = null;
            try {
                nicknames = db.getNicknamesTable().getNicknames(uuid);
                if (!nicknames.isEmpty()) {
                    return nicknames.get(nicknames.size() - 1);
                }
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
        return cached;
    }

    public void addFirstLeaveCheck(UUID uuid) {
        firstSessionInformation.put(uuid, 0);
    }

    public boolean isFirstSession(UUID uuid) {
        return firstSessionInformation.containsKey(uuid);
    }

    public void endFirstSessionActionTracking(UUID uuid) {
        firstSessionInformation.remove(uuid);
    }

    public void firstSessionMessageSent(UUID uuid) {
        Integer msgCount = firstSessionInformation.getOrDefault(uuid, 0);
        msgCount++;
        firstSessionInformation.put(uuid, msgCount);
    }

    public int getFirstSessionMsgCount(UUID uuid) {
        return firstSessionInformation.getOrDefault(uuid, 0);
    }
}
