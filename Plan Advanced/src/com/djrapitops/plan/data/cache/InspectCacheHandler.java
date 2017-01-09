package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class InspectCacheHandler {

    private DataCacheHandler handler;
    private Plan plugin;
    private HashMap<UUID, UserData> cache;

    /**
     * Class constructor
     *
     * @param plugin Current instance of Plan.class
     */
    public InspectCacheHandler(Plan plugin) {
        this.handler = plugin.getHandler();
        this.plugin = plugin;
        this.cache = new HashMap<>();
    }

    /**
     * Caches the UserData of user to the HashMap for 5 minutes. Data is removed
     * from the cache automatically after 5 minutes with a BukkitRunnable
     *
     * @param uuid UUID of the player
     */
    public void cache(UUID uuid) {
        if (!handler.getDB().wasSeenBefore(uuid)) {
            return;
        }
        cache.put(uuid, handler.getCurrentData(uuid, false));
        (new BukkitRunnable() {
            @Override
            public void run() {
                clearFomCache(uuid);
            }
        }).runTaskLater(plugin, 60 * 20 * 3);
    }

    private void clearFomCache(UUID uuid) {
        cache.remove(uuid);
    }

    /**
     * Checks the cache for UserData matching UUID
     *
     * @param uuid UUID of the Player
     * @return UserData that matches the player, null if not cached.
     */
    public UserData getFromCache(UUID uuid) {
        return cache.get(uuid);
    }

    public HashMap<UUID, UserData> getCache() {
        return cache;
    }
}
