package main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.ui.webserver.response.InspectPageResponse;
import main.java.com.djrapitops.plan.ui.webserver.response.api.JsonResponse;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.ExportUtility;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class stores UserData objects used for displaying the Html pages.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
@Deprecated
public class InspectCacheHandler {

    private final DataCache handler;
    private final Map<UUID, UserData> cache;
    private final Map<UUID, Long> cacheTimes;

    /**
     * Class constructor.
     *
     * @param plugin Current instance of Plan.class
     */
    public InspectCacheHandler(Plan plugin) {
        this.handler = plugin.getDataCache();
        this.cache = new HashMap<>();
        cacheTimes = new HashMap<>();
    }

    /**
     * Caches the UserData object to InspectCache.
     * <p>
     * If the Userdata is cached in DataCache it will be used. Otherwise the Get
     * Queue will handle the DBCallableProcessor.
     *
     * @param uuid UUID of the player.
     */
    @Deprecated // Does not cache anything anymore // TODO Remove
    public void cache(UUID uuid) {
        DBCallableProcessor cacher = data -> {
            UserData userData = new UserData(data);

            cache.put(uuid, userData);
            cacheTimes.put(uuid, MiscUtils.getTime());

            PageCacheHandler.cachePage("inspectPage: " + uuid, () -> new InspectPageResponse(Plan.getInstance().getUiServer().getDataReqHandler(), uuid));
            PageCacheHandler.cachePage("inspectionJson: " + uuid, () -> new JsonResponse(userData));

            try {
                ExportUtility.writeInspectHtml(data, ExportUtility.getPlayersFolder(ExportUtility.getFolder()), HtmlUtils.getStringFromResource("player.html"));
            } catch (FileNotFoundException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        };
    }

    /**
     * Used to cache all UserData to the InspectCache from the cache and
     * provided database.
     *
     * @param db Database to cache from if data is not in the cache.
     * @throws SQLException If Database is not properly enabled
     */
    public void cacheAllUserData(Database db) throws SQLException {
        List<UserData> userDataForUUIDS = db.getUserDataForUUIDS(db.getUsersTable().getSavedUUIDs());
        long time = MiscUtils.getTime();
        for (UserData uData : userDataForUUIDS) {
            UUID uuid = uData.getUuid();
            cache.put(uuid, new UserData(uData));
            cacheTimes.put(uuid, time);
        }
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
     * Returns the Epoch millisecond the data was cached to the inspect cache.
     *
     * @param uuid UUID of the player.
     * @return -1 when not cached or Epoch millisecond.
     */
    public long getCacheTime(UUID uuid) {
        return cacheTimes.getOrDefault(uuid, -1L);
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

    /**
     * Used to get all cached userdata objects.
     *
     * @return List of cached userdata objects.
     */
    public List<UserData> getCachedUserData() {
        return new ArrayList<>(cache.values());
    }
}
