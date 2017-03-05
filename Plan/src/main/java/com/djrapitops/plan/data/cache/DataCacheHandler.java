package main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.*;
import main.java.com.djrapitops.plan.data.handlers.*;
import main.java.com.djrapitops.plan.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Rsl1122
 */
public class DataCacheHandler {

    // Cache
    private final HashMap<UUID, UserData> dataCache;
    private HashMap<String, Integer> commandUse;

    // Plan
    private final Plan plugin;
    private final Database db;

    // Handlers
    private final ActivityHandler activityHandler;
    private final GamemodeTimesHandler gamemodeTimesHandler;
    private final LocationHandler locationHandler;
    private final DemographicsHandler demographicsHandler;
    private final BasicInfoHandler basicInfoHandler;
    private final RuleBreakingHandler ruleBreakingHandler;
    private CommandUseHandler commandUseHandler;
    private final KillHandler killHandler;
    private final SessionHandler sessionHandler;

    // Queues
    private final DataCacheSaveQueue saveTask;
    private final DataCacheClearQueue clearTask;
    private final DataCacheGetQueue getTask;

    // Variables
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
        locationHandler = new LocationHandler(plugin);
        demographicsHandler = new DemographicsHandler(plugin, this);
        basicInfoHandler = new BasicInfoHandler(plugin, this);
        ruleBreakingHandler = new RuleBreakingHandler(plugin, this);

        killHandler = new KillHandler(plugin);
        sessionHandler = new SessionHandler(plugin);

        getTask = new DataCacheGetQueue(plugin);
        clearTask = new DataCacheClearQueue(plugin, this);
        saveTask = new DataCacheSaveQueue(plugin);

        timesSaved = 0;
        maxPlayers = plugin.getServer().getMaxPlayers();
        try {
            commandUse = db.getCommandUse();
            commandUseHandler = new CommandUseHandler(commandUse);
        } catch (SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
            plugin.logError(Phrase.DB_FAILURE_DISABLE + "");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
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
                timesSaved++;
            }
        }).runTaskTimerAsynchronously(plugin, 60 * 20 * minutes, 60 * 20 * minutes);
    }

    /**
     * Uses Database to retrieve the UserData of a matching player
     *
     * Caches the data to the HashMap if cache: true
     *
     * @param processor DBCallableProcessor Object used to process the data
     * after it was retrieved
     * @param uuid Player's UUID
     * @param cache Wether or not the UserData will be Cached in this instance
     * of DataCacheHandler
     */
    public void getUserDataForProcessing(DBCallableProcessor processor, UUID uuid, boolean cache) {
        UserData uData = dataCache.get(uuid);
        if (uData == null) {
            if (cache) {
                DBCallableProcessor cacher = new DBCallableProcessor() {
                    @Override
                    public void process(UserData data) {

                        dataCache.put(uuid, data);
                        plugin.log(Phrase.CACHE_ADD.parse(uuid.toString()));

                    }
                };
                getTask.scheduleForGet(uuid, cacher, processor);
            } else {
                getTask.scheduleForGet(uuid, processor);
            }
        } else {
            processor.process(uData);
        }
    }

    /**
     ** Uses Database to retrieve the UserData of a matching player Caches the
     * data to the HashMap
     *
     * @param processor DBCallableProcessor Object used to process the data
     * after it was retrieved
     * @param uuid Player's UUID
     */
    public void getUserDataForProcessing(DBCallableProcessor processor, UUID uuid) {
        getUserDataForProcessing(processor, uuid, true);
    }

    /**
     * Saves all data in the cache to Database. Should only be called from Async
     * thread
     */
    public void saveCachedUserData() {
        List<UserData> data = new ArrayList<>();
        data.addAll(dataCache.values());
        try {
            db.saveMultipleUserData(data);
        } catch (SQLException ex) {
            plugin.toLog(this.getClass().getName(), ex);
        }
    }

    /**
     * Saves all data in the cache to Database and closes the database down.
     * Closes save clear and get tasks.
     */
    public void saveCacheOnDisable() {
        saveTask.stop();
        getTask.stop();
        clearTask.stop();
        List<UserData> data = new ArrayList<>();
        data.addAll(dataCache.values());
        data.parallelStream().forEach((userData) -> {
            sessionHandler.endSession(userData);
        });
        try {
            db.saveMultipleUserData(data);
            db.saveCommandUse(commandUse);
            db.close();
        } catch (SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * Saves the cached data of matching Player if it is in the cache
     *
     * @param uuid Player's UUID
     */
    public void saveCachedData(UUID uuid) {
        UserData data = dataCache.get(uuid);
        if (data != null) {
            saveTask.scheduleForSave(data);
        }
    }

    /**
     * Scheludes the cached CommandUsage to be saved.
     *
     */
    public void saveCommandUse() {
        try {
            db.saveCommandUse(commandUse);
        } catch (SQLException | NullPointerException e) {
            plugin.toLog(this.getClass().getName(), e);
        }
    }

    // Should only be called from Async thread

    /**
     *
     */
    public void saveHandlerDataToCache() {
        Bukkit.getServer().getOnlinePlayers().parallelStream().forEach((p) -> {
            saveHandlerDataToCache(p);
        });
    }

    // Should only be called from Async thread
    private void saveHandlerDataToCache(Player p) {
        DBCallableProcessor cacheUpdater = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                activityHandler.saveToCache(data);
                gamemodeTimesHandler.saveToCache(p.getGameMode(), data);
            }
        };
        getUserDataForProcessing(cacheUpdater, p.getUniqueId());
    }

    /**
     * Clears all UserData from the HashMap
     */
    public void clearCache() {
        clearTask.scheduleForClear(dataCache.keySet());
    }

    /**
     * Clears the matching UserData from the HashMap
     *
     * @param uuid Player's UUID
     */
    public void clearFromCache(UUID uuid) {
        dataCache.remove(uuid);
        plugin.log(Phrase.CACHE_REMOVE.parse(uuid.toString()));
    }

    /**
     *
     * @param uuid
     */
    public void scheludeForClear(UUID uuid) {
        clearTask.scheduleForClear(uuid);
    }

    /**
     *
     * @param uuid
     * @return
     */
    public boolean isDataAccessed(UUID uuid) {
        UserData userData = dataCache.get(uuid);
        if (userData != null) {
            return userData.isAccessed();
        }
        return false;
    }

    /**
     * Creates a new UserData instance and saves it to the Database
     *
     * @param player Player the new UserData is created for
     */
    public void newPlayer(Player player) {
        saveTask.scheduleNewPlayer(NewPlayerCreator.createNewPlayer(player));
    }

    /**
     *
     * @param player
     */
    public void newPlayer(OfflinePlayer player) {
        saveTask.scheduleNewPlayer(NewPlayerCreator.createNewPlayer(player));
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
     *
     * @return
     */
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

    /**
     *
     * @return
     */
    public HashMap<String, Integer> getCommandUse() {
        return commandUse;
    }

    /**
     * @return Current instance of ServerDataHandler
     */
    public CommandUseHandler getCommandUseHandler() {
        return commandUseHandler;
    }

    /**
     *
     * @return
     */
    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    /**
     * If /reload is run this treats every online player as a new login.
     *
     * Calls all the methods that are ran when PlayerJoinEvent is fired
     */
    public void handleReload() {
        BukkitTask asyncReloadCacheUpdateTask = (new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    boolean isNewPlayer = activityHandler.isFirstTimeJoin(uuid);
                    if (isNewPlayer) {
                        newPlayer(player);
                    }
                    DBCallableProcessor cacheUpdater = new DBCallableProcessor() {
                        @Override
                        public void process(UserData data) {
                            activityHandler.handleReload(data);
                            basicInfoHandler.handleReload(player.getDisplayName(), player.getAddress().getAddress(), data);
                            gamemodeTimesHandler.handleReload(player.getGameMode(), data);
                            saveCachedUserData();
                        }
                    };
                    getUserDataForProcessing(cacheUpdater, player.getUniqueId());
                }
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);        
    }

    /**
     * Used by Analysis for Player activity graphs.
     *
     * @return Maximum number of players defined in server.properties.
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }
}
