package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

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
public class DataCache extends SessionCache implements SubSystem {

    private final Map<UUID, String> playerNames;
    private final Map<String, UUID> uuids;
    private final Map<UUID, String> displayNames;
    private Database db;

    public DataCache(PlanSystem system) {
        super(system);

        playerNames = new HashMap<>();
        displayNames = new HashMap<>();
        uuids = new HashMap<>();
    }

    public static DataCache getInstance() {
        DataCache dataCache = CacheSystem.getInstance().getDataCache();
        Verify.nullCheck(dataCache, () -> new IllegalStateException("Data Cache was not initialized."));
        return dataCache;
    }

    @Override
    public void enable() {
        db = system.getDatabaseSystem().getActiveDatabase();
    }

    @Override
    public void disable() {
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

    /**
     * Used to get the player name in the cache.
     * <p>
     * It is recommended to use
     * {@link com.djrapitops.plan.data.store.keys.AnalysisKeys#PLAYER_NAMES} and
     * {@link com.djrapitops.plan.data.store.keys.PlayerKeys#NAME} when possible
     * because this method will call database if a name is not found.
     *
     * @param uuid UUID of the player.
     * @return name or null if not cached.
     */
    public String getName(UUID uuid) {
        String name = playerNames.get(uuid);
        if (name == null) {
            try {
                name = db.fetch().getPlayerName(uuid);
                playerNames.put(uuid, name);
            } catch (DBOpException e) {
                Log.toLog(this.getClass(), e);
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
                nicknames = db.fetch().getNicknames(uuid);
                if (!nicknames.isEmpty()) {
                    return nicknames.get(nicknames.size() - 1);
                }
            } catch (DBOpException e) {
                Log.toLog(this.getClass(), e);
            }
        }
        return cached;
    }

    public Set<UUID> getUuids() {
        return playerNames.keySet();
    }

    public UUID getUUIDof(String playerName) {
        return uuids.get(playerName);
    }
}
