package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.data.*;
import com.djrapitops.plan.data.handlers.*;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Rsl1122
 */
public class DataCacheHandler {

    private final HashMap<UUID, UserData> dataCache;
    private final Plan plugin;
    private final ActivityHandler activityHandler;
    private final GamemodeTimesHandler gamemodeTimesHandler;
    private final LocationHandler locationHandler;
    private final DemographicsHandler demographicsHandler;
    private final BasicInfoHandler basicInfoHandler;
    private final RuleBreakingHandler ruleBreakingHandler;
    private final ServerData serverData;
    private final ServerDataHandler serverDataHandler;
    private final PlanLiteHandler planLiteHandler;
    private final Database db;
    private final NewPlayerCreator newPlayerCreator;

    private int timesSaved;

    /**
     * Class Constructor
     *
     * Creates the set of Handlers that will be used to modify UserData gets the
     * Database from the plugin
     *
     * @param plugin Current instance of Plan
     */
    public DataCacheHandler(Plan plugin) {
        this.plugin = plugin;
        db = plugin.getDB();
        dataCache = new HashMap<>();
        activityHandler = new ActivityHandler(plugin, this);
        gamemodeTimesHandler = new GamemodeTimesHandler(plugin, this);
        locationHandler = new LocationHandler(plugin, this);
        demographicsHandler = new DemographicsHandler(plugin, this);
        basicInfoHandler = new BasicInfoHandler(plugin, this);
        ruleBreakingHandler = new RuleBreakingHandler(plugin, this);
        serverData = db.getNewestServerData();
        serverDataHandler = new ServerDataHandler(serverData);
        planLiteHandler = new PlanLiteHandler(plugin);
        newPlayerCreator = new NewPlayerCreator(plugin, this);

        timesSaved = 0;

        int minutes = plugin.getConfig().getInt("saveEveryXMinutes");
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                saveHandlerDataToCache();
                saveCachedData();
                if (timesSaved % 5 == 0) {
                    clearCache();
                }
            }
        }, 60 * 20 * minutes, 60 * 20 * minutes);
    }

    /**
     * Tells wether or not user has been saved to the database before
     *
     * @param uuid Players UUID
     * @return User's data is not in the database: true
     * @deprecated Moved to ActivityHandler
     */
    @Deprecated
    public boolean isFirstTimeJoin(UUID uuid) {
        return activityHandler.isFirstTimeJoin(uuid);
    }

    /**
     * Uses Database to retrieve the UserData of a matching player
     *
     * Caches the data to the HashMap if cache: true
     *
     * @param uuid Player's UUID
     * @param cache Wether or not the UserData will be Cached in this instance
     * of DataCacheHandler
     * @return UserData matching the Player
     */
    public UserData getCurrentData(UUID uuid, boolean cache) {
        if (cache) {
            if (dataCache.get(uuid) == null) {
                dataCache.put(uuid, db.getUserData(uuid));
            }
            return dataCache.get(uuid);
        } else {
            return db.getUserData(uuid);
        }
    }

    /**
     ** Uses Database to retrieve the UserData of a matching player Caches the
     * data to the HashMap
     *
     * @param uuid Player's UUID
     * @return UserData matching the Player
     */
    public UserData getCurrentData(UUID uuid) {
        return getCurrentData(uuid, true);
    }

    /**
     * Saves all data in the cache to Database with AsyncTasks
     */
    public void saveCachedData() {
        dataCache.keySet().parallelStream().forEach((uuid) -> {
            saveCachedData(uuid);
        });
        saveServerData();
        timesSaved++;
    }

    /**
     * Saves all data in the cache to Database without AsyncTask (Disabled
     * plugins can't register tasks)
     */
    public void saveCacheOnDisable() {
        dataCache.keySet().stream().forEach((uuid) -> {
            if (dataCache.get(uuid) != null) {
                db.saveUserData(uuid, dataCache.get(uuid));
            }
        });
        db.saveServerData(serverData);
    }

    /**
     * Saves the cached data of matching Player if it is in the cache
     *
     * @param uuid Player's UUID
     */
    public void saveCachedData(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (dataCache.get(uuid) != null) {
                    db.saveUserData(uuid, dataCache.get(uuid));
                }
            }
        });
    }

    /**
     * Saves the cached ServerData with AsyncTask
     *
     * Data is saved on a new line with a long value matching current Date
     */
    public void saveServerData() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                db.saveServerData(serverData);
            }
        });
    }

    private void saveHandlerDataToCache() {
        Bukkit.getServer().getOnlinePlayers().parallelStream().forEach((p) -> {
            UserData data = getCurrentData(p.getUniqueId());
            activityHandler.saveToCache(p, data);
            gamemodeTimesHandler.saveToCache(p, data);
        });
    }

    /**
     * Clears all UserData from the HashMap
     */
    public void clearCache() {
        dataCache.clear();
    }

    /**
     * Clears the matching UserData from the HashMap
     *
     * @param uuid Player's UUID
     */
    public void clearFromCache(UUID uuid) {
        if (dataCache.get(uuid) != null) {
            dataCache.remove(uuid);
        }
    }

    /**
     * Creates a new UserData instance and saves it to the Database
     *
     * @param player Player the new UserData is created for
     */
    public void newPlayer(Player player) {
        newPlayerCreator.createNewPlayer(player);
    }

    /**
     * @return The HashMap containing all Cached UserData
     */
    public HashMap<UUID, UserData> getDataCache() {
        return dataCache;
    }

    /**
     * @return Current instance of the ActivityHandler
     */
    public ActivityHandler getActivityHandler() {
        return activityHandler;
    }

    /**
     * @return Current instance of the LocationHandler
     */
    public LocationHandler getLocationHandler() {
        return locationHandler;
    }

    /**
     * @return Current instance of the DemographicsHandler
     */
    public DemographicsHandler getDemographicsHandler() {
        return demographicsHandler;
    }

    /**
     * @return Current instance of the BasicInfoHandler
     */
    public BasicInfoHandler getBasicInfoHandler() {
        return basicInfoHandler;
    }

    /**
     * @return Current instance of the RuleBreakingHandler
     */
    public RuleBreakingHandler getRuleBreakingHandler() {
        return ruleBreakingHandler;
    }

    /**
     * @return Current instance of the GamemodeTimesHandler
     */
    public GamemodeTimesHandler getGamemodeTimesHandler() {
        return gamemodeTimesHandler;
    }

    /**
     * Returns the same value as Plan#getDB().
     *
     * @return Current instance of the Database,
     */
    public Database getDB() {
        return db;
    }

    /**
     * Updates the player count and returns cached ServerData.
     *
     * @return Cached serverData
     */
    public ServerData getServerData() {
        serverData.updatePlayerCount();
        return serverData;
    }

    /**
     * @return Current instance of ServerDataHandler
     */
    public ServerDataHandler getServerDataHandler() {
        return serverDataHandler;
    }

    /**
     * If /reload is run this treats every online player as a new login.
     *
     * Calls all the methods that are ran when PlayerJoinEvent is fired
     */
    public void handleReload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            boolean isNewPlayer = activityHandler.isFirstTimeJoin(uuid);
            if (isNewPlayer) {
                newPlayer(player);
            }
            serverDataHandler.handleLogin(isNewPlayer);
            UserData data = getCurrentData(uuid);
            activityHandler.handleReload(player, data);
            basicInfoHandler.handleReload(player, data);
            gamemodeTimesHandler.handleReload(player, data);
            saveCachedData(uuid);
        }
    }
}
