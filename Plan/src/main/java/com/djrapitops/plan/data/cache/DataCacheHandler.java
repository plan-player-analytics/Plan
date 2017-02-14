package main.java.com.djrapitops.plan.data.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.*;
import main.java.com.djrapitops.plan.data.handlers.*;
import main.java.com.djrapitops.plan.database.Database;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    private final HashMap<String, Integer> commandUse;
    private final CommandUseHandler commandUseHandler;
    private final PlanLiteHandler planLiteHandler;
    private final KillHandler killHandler;
    private final SessionHandler sessionHandler;
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
        commandUse = db.getCommandUse();
        commandUseHandler = new CommandUseHandler(commandUse);
        planLiteHandler = new PlanLiteHandler(plugin);
        newPlayerCreator = new NewPlayerCreator(plugin, this);
        killHandler = new KillHandler(plugin);
        sessionHandler = new SessionHandler(plugin);

        timesSaved = 0;
        maxPlayers = plugin.getServer().getMaxPlayers();

        int minutes = Settings.SAVE_CACHE_MIN.getNumber();
        if (minutes <= 0) {
            minutes = 5;
        }
        final int clearAfterXsaves;
        int configValue = Settings.CLEAR_CACHE_X_SAVES.getNumber();
        if (configValue <= 1) {
            clearAfterXsaves = 2;
        } else {
            clearAfterXsaves = configValue;
        }
        BukkitTask asyncPeriodicCacheSaveTask = (new BukkitRunnable() {
            @Override
            public void run() {
                DataCacheHandler handler = plugin.getHandler();
                handler.saveHandlerDataToCache();
                handler.saveCachedUserData();
                if (timesSaved % clearAfterXsaves == 0) {
                    handler.clearCache();
                }
                saveCommandUse();
                handler.clearNulls();
                timesSaved++;
            }
        }).runTaskTimerAsynchronously(plugin, 60 * 20 * minutes, 60 * 20 * minutes);
//        (new BukkitRunnable() {
//            @Override
//            public void run() {
//                
//            }
//        }).runTaskTimerAsynchronously(plugin, 60 * 20 * sMinutes, 60 * 20 * sMinutes);
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
                UserData uData = db.getUserData(uuid);
                if (uData != null) {
                    if (uData.getPlanLiteData() == null) {
                        getPlanLiteHandler().handleEvents(uData.getName(), uData);
                    }
                    dataCache.put(uuid, uData);
                    plugin.log(Phrase.ADD_TO_CACHE.parse(uuid.toString()));
                }
            }
            return dataCache.get(uuid);
        } else {
            if (dataCache.get(uuid) != null) {
                return dataCache.get(uuid);
            }
            UserData uData = db.getUserData(uuid);
            if (uData != null) {
                if (uData.getPlanLiteData() == null) {
                    getPlanLiteHandler().handleEvents(uData.getName(), uData);
                }
            }
            return uData;
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
    public void saveCachedUserData() {
        BukkitTask asyncFullCacheSaveTask = (new BukkitRunnable() {
            @Override
            public void run() {
                List<UserData> data = new ArrayList<>();
                data.addAll(dataCache.values());
                db.saveMultipleUserData(data);
                timesSaved++;
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);
    }

    /**
     * Saves all data in the cache to Database and closes the database down.
     */
    public void saveCacheOnDisable() {
        List<UserData> data = new ArrayList<>();
        data.addAll(dataCache.values());
        data.parallelStream().forEach((userData) -> {
            sessionHandler.endSession(userData);
        });
        db.saveMultipleUserData(data);
        db.saveCommandUse(commandUse);
        db.close();
    }

    /**
     * Saves the cached data of matching Player if it is in the cache
     *
     * @param uuid Player's UUID
     */
    public void saveCachedData(UUID uuid) {
        BukkitTask asyncCachedUserSaveTask = (new BukkitRunnable() {
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
    public void saveCommandUse() {
        BukkitTask asyncCommandUseSaveTask = (new BukkitRunnable() {
            @Override
            public void run() {
                db.saveCommandUse(commandUse);
            }
        }).runTaskAsynchronously(plugin);
    }

    private void saveHandlerDataToCache() {
        Bukkit.getServer().getOnlinePlayers().parallelStream().forEach((p) -> {
            saveHandlerDataToCache(p);
        });
    }

    /**
     * Saves a single player's data to the cache from the handler if the player
     * is online.
     *
     * @param uuid UUID of the Player to save
     */
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
        Iterator<Map.Entry<UUID, UserData>> clearIterator = dataCache.entrySet().iterator();
        while (clearIterator.hasNext()) {
            clearFromCache(clearIterator.next());
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
     * Setting entry value to null, clearing it from memory.
     *
     * This method is used to avoid ConcurrentModificationException when
     * clearing the cache.
     *
     * @param entry An entry from the cache HashMap that is being iterated over.
     */
    public void clearFromCache(Map.Entry<UUID, UserData> entry) {
        if (entry != null) {
            if (entry.getValue() != null) {
                if (entry.getValue().isAccessed()) {
                    (new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (entry.getValue().isAccessed()) {
                                entry.setValue(null);
                                plugin.log("Cleared " + entry.getKey().toString() + " from Cache. (Delay task)");
                                this.cancel();
                            }
                        }
                    }).runTaskTimer(plugin, 30 * 20, 30 * 20);
                } else {
                    entry.setValue(null);
                    plugin.log("Cleared " + entry.getKey().toString() + " from Cache.");
                }
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

    public void newPlayer(OfflinePlayer player) {
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
     * @return Current instance of PlanLiteHandler
     */
    public PlanLiteHandler getPlanLiteHandler() {
        return planLiteHandler;
    }

    public KillHandler getKillHandler() {
        return killHandler;
    }

    /**
     * Returns the same value as Plan#getDB().
     *
     * @return Current instance of the Database,
     */
    public Database getDB() {
        return db;
    }

    public HashMap<String, Integer> getCommandUse() {
        return commandUse;
    }

    /**
     * @return Current instance of ServerDataHandler
     */
    public CommandUseHandler getServerDataHandler() {
        return commandUseHandler;
    }

    public SessionHandler getSessionHandler() {
        return sessionHandler;
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
            UserData data = getCurrentData(uuid);
            activityHandler.handleReload(player, data);
            basicInfoHandler.handleReload(player, data);
            gamemodeTimesHandler.handleReload(player, data);
        }
        saveCachedUserData();
    }

    /**
     * Used by Analysis for Player activity graphs.
     *
     * @return Maximum number of players defined in server.properties.
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    private void clearNulls() {
        Iterator<UUID> clearIterator = dataCache.keySet().iterator();
        while (clearIterator.hasNext()) {
            if (dataCache.get(clearIterator.next()) == null) {
                clearIterator.remove();
            }
        }
    }
}
