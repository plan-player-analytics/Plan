package main.java.com.djrapitops.plan.systems.cache;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;

import java.sql.SQLException;
import java.util.*;

/**
 * This Class contains the Cache.
 * <p>
 * Contains:
 * <ul>
 * <li>PlayerName cache, used for reducing database calls on chat events</li>
 * <li>DisplayName cache, used for reducing database calls on chat events</li>
 * <li>FirstSession MessageCount Map, used for tracking first session and message count on that session.</li>
 * </ul>
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class DataCache extends SessionCache {

    private static final Map<UUID, Integer> firstSessionInformation = new HashMap<>();
    private final Database db;
    private final Map<UUID, String> playerNames;
    private final Map<String, UUID> uuids;
    private final Map<UUID, String> displayNames;

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
        uuids = new HashMap<>();
    }

    /**
     * Used to update PlayerName and DisplayName caches.
     *
     * @param uuid        UUID of the player.
     * @param playerName  Name of the player.
     * @param displayName DisplayName of the player.
     */
    public void updateNames(UUID uuid, String playerName, String displayName) {
        if (playerName != null) {
            playerNames.put(uuid, playerName);
            uuids.put(playerName, uuid);
        }
        if (displayName != null) {
            displayNames.put(uuid, displayName);
        }
    }

    public void cacheSavedNames() {
        try {
            Map<UUID, String> playerNames = db.getUsersTable().getPlayerNames();
            this.playerNames.putAll(playerNames);
            for (Map.Entry<UUID, String> entry : playerNames.entrySet()) {
                uuids.put(entry.getValue(), entry.getKey());
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * Used to get the player name in the cache.
     *
     * @param uuid UUID of the player.
     * @return name or null if not cached.
     */
    public String getName(UUID uuid) {
        String name = playerNames.get(uuid);
        if (name == null) {
            try {
                name = db.getUsersTable().getPlayerName(uuid);
                playerNames.put(uuid, name);
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
                name = "Error occurred";
            }
        }
        return name;
    }

    /**
     * Used to get the player display name in the cache.
     * <p>
     * If not cached, one from the database will be cached.
     *
     * @param uuid UUID of the player.
     * @return latest displayName or null if none are saved.
     */
    public String getDisplayName(UUID uuid) {
        String cached = displayNames.get(uuid);
        if (cached == null) {
            List<String> nicknames;
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
     * Condition if a session is player's first session on the server.
     *
     * @param uuid UUID of the player
     * @return true / false
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

    public Set<UUID> getUuids() {
        return playerNames.keySet();
    }

    public Map<UUID, Integer> getFirstSessionMsgCounts() {
        return firstSessionInformation;
    }

    public UUID getUUIDof(String playerName) {
        return uuids.get(playerName);
    }
}
