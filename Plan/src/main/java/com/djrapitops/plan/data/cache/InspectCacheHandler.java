package main.java.com.djrapitops.plan.data.cache;

import java.util.HashMap;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;

/**
 * This class stores UserData objects used for displaying the Html pages.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class InspectCacheHandler {

    private DataCacheHandler handler;
    private Plan plugin;
    private HashMap<UUID, UserData> cache;

    /**
     * Class constructor.
     *
     * @param plugin Current instance of Plan.class
     */
    public InspectCacheHandler(Plan plugin) {
        this.handler = plugin.getHandler();
        this.plugin = plugin;
        this.cache = new HashMap<>();
    }

    /**
     * Caches the UserData object to InspectCache.
     *
     * If the Userdata is cached in DataCache it will be used. Otherwise the Get
     * Queue will handle the DBCallableProcessor.
     *
     * @param uuid UUID of the player.
     */
    public void cache(UUID uuid) {
        DBCallableProcessor cacher = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                cache.put(uuid, new UserData(data));
            }
        };
        handler.getUserDataForProcessing(cacher, uuid, false);
    }

    /**
     * Checks the cache for UserData matching UUID.
     *
     * @param uuid UUID of the Player
     * @return UserData that matches the player, null if not cached.
     */
    public UserData getFromCache(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Check if the data of a player is in the inspect cache.
     *
     * @param uuid UUID of player.
     * @return true if cached.
     */
    public boolean isCached(UUID uuid) {
        return cache.containsKey(uuid);
    }
}
