package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.api.utility.log.Log;

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

    private Database db;
    private final Map<UUID, String> playerNames;
    private final Map<String, UUID> uuids;
    private final Map<UUID, String> displayNames;

    public DataCache(PlanSystem system) {
        super(system);

        playerNames = new HashMap<>();
        displayNames = new HashMap<>();
        uuids = new HashMap<>();
    }

    @Override
    public void enable() {
        db = system.getDatabaseSystem().getActiveDatabase();
    }

    @Override
    public void disable() {
    }

    public static DataCache getInstance() {
        DataCache dataCache = CacheSystem.getInstance().getDataCache();
        NullCheck.check(dataCache, new IllegalStateException("Data Cache was not initialized."));
        return dataCache;
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
            Map<UUID, String> playerNames = db.fetch().getPlayerNames();
            this.playerNames.putAll(playerNames);
            for (Map.Entry<UUID, String> entry : playerNames.entrySet()) {
                uuids.put(entry.getValue(), entry.getKey());
            }
        } catch (DBException e) {
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
                name = db.fetch().getPlayerName(uuid);
                playerNames.put(uuid, name);
            } catch (DBException e) {
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
                nicknames = db.fetch().getNicknames(uuid);
                if (!nicknames.isEmpty()) {
                    return nicknames.get(nicknames.size() - 1);
                }
            } catch (DBException e) {
                Log.toLog(this.getClass().getName(), e);
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
