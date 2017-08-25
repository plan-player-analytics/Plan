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
 * Contains:
 * <ul>
 *     <li>PlayerName cache, used for reducing database calls on chat events</li>
 *     <li>DisplayName cache, used for reducing database calls on chat events</li>
 *     <li>FirstSession MessageCount Map, used for tracking first session & message count on that session.</li>
 * </ul>
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class DataCache extends SessionCache {

    private final Database db;

    private final Map<UUID, String> playerNames;
    private final Map<UUID, String> displayNames;

    private static final Map<UUID, Integer> firstSessionInformation = new HashMap<>();

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public DataCache(Plan plugin) {
        super(plugin);
        db = plugin.getDB();

        playerNames = new HashMap<>();
        displayNames = new HashMap<>();
    }

    /**
     * Used to update PlayerName and DisplayName caches.
     *
     * @param uuid UUID of the player.
     * @param playerName Name of the player.
     * @param displayName DisplayName of the player.
     */
    public void updateNames(UUID uuid, String playerName, String displayName) {
        playerNames.put(uuid, playerName);
        displayNames.put(uuid, displayName);
    }

    /**
     * Used to get the player name in the cache.
     *
     * @param uuid UUID of the player.
     * @return name or null if not cached.
     */
    public String getName(UUID uuid) {
        return playerNames.get(uuid);
    }

    /**
     * Used to get the player display name in the cache.
     *
     * If not cached, one from the database will be cached.
     *
     * @param uuid UUID of the player.
     * @return latest displayName or null if none are saved.
     */
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

    /**
     * Used for marking first Session Actions to be saved.
     *
     * @param uuid UUID of the new player.
     */
    public void markFirstSession(UUID uuid) {
        firstSessionInformation.put(uuid, 0);
    }

    /**
     *
     * @param uuid
     * @return
     */
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
