package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.data.*;
import com.djrapitops.plan.data.handlers.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import static org.bukkit.Bukkit.getPlayer;

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
    private int maxPlayers;

    /**
     * Class Constructor.
     *
     * Creates the set of Handlers that will be used to modify UserData. Gets
     * the Database from the plugin. Registers Asyncronous Periodic Save Task
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
        maxPlayers = plugin.getServer().getMaxPlayers();

        int minutes = plugin.getConfig().getInt("Settings.Cache.DataCache.SaveEveryXMinutes");
        if (minutes <= 0) {
            minutes = 5;
        }
        final int clearAfterXsaves;
        int configValue = plugin.getConfig().getInt("Settings.Cache.DataCache.ClearCacheEveryXSaves");
        if (configValue <= 1) {
            clearAfterXsaves = 2;
        } else {
            clearAfterXsaves = configValue;
        }
        (new BukkitRunnable() {
            @Override
            public void run() {
                DataCacheHandler handler = plugin.getHandler();
                handler.saveHandlerDataToCache();
                handler.saveCachedData();
                if (timesSaved % clearAfterXsaves == 0) {
                    handler.clearCache();
                }
                timesSaved++;
            }
        }).runTaskTimerAsynchronously(plugin, 60 * 20 * minutes, 60 * 20 * minutes);
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
                plugin.log("Added " + uuid.toString() + " to Cache.");
            }
            return dataCache.get(uuid);
        } else {
            if (dataCache.get(uuid) != null) {
                return dataCache.get(uuid);
            }
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
        dataCache.keySet().stream().forEach((uuid) -> {
            saveCachedData(uuid);
        });
        saveServerData();
        timesSaved++;
    }

    /**
     * Saves all data in the cache to Database and closes the database down.
     */
    public void saveCacheOnDisable() {
        dataCache.keySet().stream().forEach((uuid) -> {
            if (dataCache.get(uuid) != null) {
                db.saveUserData(uuid, dataCache.get(uuid));
            }
        });
        db.saveServerData(serverData);
        db.close();
    }

    /**
     * Saves the cached data of matching Player if it is in the cache
     *
     * @param uuid Player's UUID
     */
    public void saveCachedData(UUID uuid) {
        (new BukkitRunnable() {
            @Override
            public void run() {
                if (dataCache.get(uuid) != null) {
                    db.saveUserData(uuid, dataCache.get(uuid));
                }
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);

    }

    /**
     * Saves the cached ServerData with AsyncTask
     *
     * Data is saved on a new line with a long value matching current Date
     */
    public void saveServerData() {
        (new BukkitRunnable() {
            @Override
            public void run() {
                db.saveServerData(serverData);
            }
        }).runTaskAsynchronously(plugin);
    }

    private void saveHandlerDataToCache() {
        Bukkit.getServer().getOnlinePlayers().parallelStream().forEach((p) -> {
            saveHandlerDataToCache(p);
        });
    }

    public void saveHandlerDataToCache(UUID uuid) {
        Player p = getPlayer(uuid);
        if (p != null) {
            if (p.isOnline()) {
                saveHandlerDataToCache(p);
            }
        }
    }

    private void saveHandlerDataToCache(Player p) {
        UserData data = getCurrentData(p.getUniqueId());
        activityHandler.saveToCache(p, data);
        gamemodeTimesHandler.saveToCache(p, data);
    }

    /**
     * Clears all UserData from the HashMap
     */
    public void clearCache() {
        Set<UUID> uuidSet = dataCache.keySet();
        Iterator<UUID> uuidIterator = uuidSet.iterator();
        while (uuidIterator.hasNext()) {
            clearFromCache(uuidIterator.next());
        }
    }

    /**
     * Clears the matching UserData from the HashMap
     *
     * @param uuid Player's UUID
     */
    public void clearFromCache(UUID uuid) {
        if (dataCache.get(uuid) != null) {
            if (dataCache.get(uuid).isAccessed()) {
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!dataCache.get(uuid).isAccessed()) {
                            dataCache.remove(uuid);
                            plugin.log("Cleared " + uuid.toString() + " from Cache. (Delay task)");
                            this.cancel();
                        }
                    }
                }).runTaskTimer(plugin, 30 * 20, 30 * 20);
            } else {
                dataCache.remove(uuid);
                plugin.log("Cleared " + uuid.toString() + " from Cache.");
            }
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

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
