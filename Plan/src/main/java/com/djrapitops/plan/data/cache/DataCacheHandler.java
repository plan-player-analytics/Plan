package main.java.com.djrapitops.plan.data.cache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.*;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheClearQueue;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheGetQueue;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheProcessQueue;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheSaveQueue;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.LogoutInfo;
import main.java.com.djrapitops.plan.data.handling.info.ReloadInfo;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;
import main.java.com.djrapitops.plan.utilities.comparators.HandlingInfoTimeComparator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 * This Class contains the Cache.
 *
 * This class is the main processing class that initializes Save, Clear, Process
 * and Get queue and Starts the asyncronous save task.
 *
 * It is used to store commanduse, locations, active sessions and UserData objects
 * in memory.
 *
 * It's methods can be used to access all the data it stores and to clear them.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class DataCacheHandler extends LocationCache {

    // Cache
    private final HashMap<UUID, UserData> dataCache;
    private HashMap<String, Integer> commandUse;

    // Plan
    private final Plan plugin;
    private final Database db;

    // Queues
    private DataCacheSaveQueue saveTask;
    private DataCacheClearQueue clearTask;
    private DataCacheProcessQueue processTask;
    private DataCacheGetQueue getTask;

    // Variables
    private int timesSaved;
    private int maxPlayers;

    /**
     * Class Constructor.
     *
     * Gets the Database from the plugin. Starts the queues. Registers
     * Asyncronous Periodic Save Task
     *
     * @param plugin Current instance of Plan
     */
    public DataCacheHandler(Plan plugin) {
        super();
        this.plugin = plugin;
        db = plugin.getDB();
        dataCache = new HashMap<>();

        startQueues();

        timesSaved = 0;
        maxPlayers = plugin.getServer().getMaxPlayers();

        commandUse = new HashMap<>();
        if (!getCommandUseFromDb()) {
            Log.error(Phrase.DB_FAILURE_DISABLE + "");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        startAsyncPeriodicSaveTask();
    }

    /**
     * Used to get the initial commandUse Map from the database.
     *
     * @return Was the fetch successful?
     */
    public boolean getCommandUseFromDb() {
        try {
            commandUse = db.getCommandUse();
            return true;
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return false;
    }

    /**
     * Used to start all processing Queue Threads.
     */
    public void startQueues() {
        clearTask = new DataCacheClearQueue(this);
        saveTask = new DataCacheSaveQueue(plugin, clearTask);
        getTask = new DataCacheGetQueue(plugin);
        processTask = new DataCacheProcessQueue(this);
    }

    /**
     * Used to start the Asyncronous Save Task.
     *
     * @throws IllegalArgumentException BukkitRunnable was given wrong
     * parameters.
     * @throws IllegalStateException BukkitScheduler is in a wrong state.
     */
    public void startAsyncPeriodicSaveTask() throws IllegalArgumentException, IllegalStateException {
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
        BukkitTask asyncPeriodicCacheSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                DataCacheHandler handler = Plan.getInstance().getHandler();
                handler.saveHandlerDataToCache();
                handler.saveCachedUserData();
                if (timesSaved % clearAfterXsaves == 0) {
                    handler.clearCache();
                }
                saveCommandUse();
                timesSaved++;
            }
        }.runTaskTimerAsynchronously(plugin, 60 * 20 * minutes, 60 * 20 * minutes);
    }

    /**
     * Uses Database or Cache to retrieve the UserData of a matching player.
     *
     * Caches the data to the Cache if cache-parameter is true.
     *
     * @param processor DBCallableProcessor Object used to process the data
     * after it was retrieved
     * @param uuid Player's UUID
     * @param cache Whether or not the UserData will be Cached in this instance
     * of DataCacheHandler after it has been fetched (if not already fetched)
     */
    public void getUserDataForProcessing(DBCallableProcessor processor, UUID uuid, boolean cache) {
        Log.debug(uuid + ": HANDLER getForProcess," + " Cache:" + cache);
        UserData uData = dataCache.get(uuid);
        if (uData == null) {
            if (cache) {
                DBCallableProcessor cacher = new DBCallableProcessor() {
                    @Override
                    public void process(UserData data) {
                        cache(data);
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
     * Used to Cache a UserData object to the Cache.
     *
     * If a object already exists it will be replaced.
     *
     * @param data UserData object with the UUID inside used as key.
     */
    public void cache(UserData data) {
        dataCache.put(data.getUuid(), data);
        Log.info(Phrase.CACHE_ADD.parse(data.getUuid().toString()));
    }

    /**
     * Uses Database or Cache to retrieve the UserData of a matching player.
     *
     * Always Caches the data after retrieval (unless already cached)
     *
     * @param processor DBCallableProcessor Object used to process the data
     * after it was retrieved
     * @param uuid Player's UUID
     */
    public void getUserDataForProcessing(DBCallableProcessor processor, UUID uuid) {
        getUserDataForProcessing(processor, uuid, true);
    }

    /**
     * Saves all UserData in the cache to Database.
     *
     * ATTENTION: TODO - Doesn't save the Locations in the locationCache.
     *
     * Should only be called from Async thread
     */
    public void saveCachedUserData() {
        List<UserData> data = new ArrayList<>();
        data.addAll(dataCache.values());
        try {
            db.saveMultipleUserData(data);
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
        }
    }

    /**
     * Used to add event HandlingInfo to the processTask's pool.
     *
     * Given HandlingInfo object's process method will be called.
     *
     * @param i Object that extends HandlingInfo.
     */
    public void addToPool(HandlingInfo i) {
        Log.debug(i.getUuid() + ": Adding to pool, type:" + i.getType().name());
        processTask.addToPool(i);
    }

    /**
     * Saves all data in the cache to Database and closes the database down.
     *
     * Stops all tasks.
     *
     * If processTask has unprocessed information, it will be processed.
     */
    public void saveCacheOnDisable() {
        long time = MiscUtils.getTime();
        Log.debug("SaveCacheOnDisable! " + time);
        saveTask.stop();
        getTask.stop();
        clearTask.stop();
        List<HandlingInfo> toProcess = processTask.stop();
        Log.debug("ToProcess size: " + toProcess.size() + " DataCache size: " + dataCache.keySet().size());
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Log.debug("Online: " + onlinePlayers.size());
        for (Player p : onlinePlayers) {
            UUID uuid = p.getUniqueId();
            endSession(uuid);
            if (dataCache.containsKey(uuid)) {
                dataCache.get(uuid).addLocations(getLocationsForSaving(uuid));
            }
            toProcess.add(new LogoutInfo(uuid, time, p.isBanned(), p.getGameMode(), getSession(uuid)));
        }
        Log.debug("ToProcess size_AFTER: " + toProcess.size() + " DataCache size: " + dataCache.keySet().size());
        Collections.sort(toProcess, new HandlingInfoTimeComparator());
        processUnprocessedHandlingInfo(toProcess);
        List<UserData> data = new ArrayList<>();
        data.addAll(dataCache.values());
        Log.debug("SAVING, DataCache size: " + dataCache.keySet().size());
        try {
            db.saveMultipleUserData(data);
            db.saveCommandUse(commandUse);
            db.close();
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        Log.debug("SaveCacheOnDisable_END");
    }

    private void processUnprocessedHandlingInfo(List<HandlingInfo> toProcess) {
        Log.debug("PROCESS: " + toProcess.size());
        for (HandlingInfo i : toProcess) {
            UserData uData = dataCache.get(i.getUuid());
            if (uData == null) {
                DBCallableProcessor p = new DBCallableProcessor() {
                    @Override
                    public void process(UserData data) {
                        i.process(data);
                    }
                };
                getUserDataForProcessing(p, i.getUuid());
            } else {
                i.process(uData);
            }
        }
    }

    /**
     * Saves the cached data of matching Player if it is in the cache.
     *
     * @param uuid Player's UUID
     */
    public void saveCachedData(UUID uuid) {
        Log.debug(uuid + ": SaveCachedData");
        DBCallableProcessor saveProcessor = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                data.addLocations(getLocationsForSaving(uuid));
                clearLocations(uuid);
                data.access();
                data.setClearAfterSave(true);
                saveTask.scheduleForSave(data);
            }
        };
        getUserDataForProcessing(saveProcessor, uuid);
    }

    /**
     * Saves the cached CommandUse.
     *
     * Should be only called from an Asyncronous Thread.
     */
    public void saveCommandUse() {
        try {
            db.saveCommandUse(commandUse);
        } catch (SQLException | NullPointerException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * Refreshes the calculations for all online players with ReloadInfo.
     */
    public void saveHandlerDataToCache() {
        Bukkit.getServer().getOnlinePlayers().parallelStream().forEach((p) -> {
            saveHandlerDataToCache(p);
        });
    }

    private void saveHandlerDataToCache(Player player) {
        long time = MiscUtils.getTime();
        UUID uuid = player.getUniqueId();
        addToPool(new ReloadInfo(uuid, time, player.getAddress().getAddress(), player.isBanned(), player.getDisplayName(), player.getGameMode()));
    }

    /**
     * Schedules all UserData from the Cache to be cleared.
     */
    public void clearCache() {
        clearTask.scheduleForClear(dataCache.keySet());
    }

    /**
     * Clears the matching UserData from the Cache if they're not online.
     *
     * @param uuid Player's UUID
     */
    public void clearFromCache(UUID uuid) {
        Log.debug(uuid + ": Clear");
        if (getOfflinePlayer(uuid).isOnline()) {
            Log.debug(uuid + ": Online, did not clear");
            UserData data = dataCache.get(uuid);
            if (data != null) {
                data.setClearAfterSave(false);
            }
        } else {
            dataCache.remove(uuid);
            Log.info(Phrase.CACHE_REMOVE.parse(uuid.toString()));
        }
    }

    /**
     * Schedules a matching UserData object to be cleared from the cache.
     *
     * @param uuid Player's UUID.
     */
    public void scheludeForClear(UUID uuid) {
        clearTask.scheduleForClear(uuid);
    }

    /**
     * Check whether or not the UserData object is being accessed by save or
     * process tasks.
     *
     * @param uuid Player's UUID
     * @return true/false
     */
    public boolean isDataAccessed(UUID uuid) {
        UserData userData = dataCache.get(uuid);
        if (userData == null) {
            return false;
        }
        boolean isAccessed = (userData.isAccessed()) || saveTask.containsUUID(uuid) || processTask.containsUUID(uuid);
        if (isAccessed) {
            userData.setClearAfterSave(false);
        }
        return isAccessed;
    }

    /**
     * Creates a new UserData instance and saves it to the Database.
     *
     * @param player Player the new UserData is created for
     */
    public void newPlayer(Player player) {
        newPlayer(NewPlayerCreator.createNewPlayer(player));
    }

    /**
     * Creates a new UserData instance and saves it to the Database.
     *
     * @param player Player the new UserData is created for
     */
    public void newPlayer(OfflinePlayer player) {
        newPlayer(NewPlayerCreator.createNewPlayer(player));
    }

    /**
     * Schedules a new player's data to be saved to the Database.
     *
     * @param data UserData object to schedule for save.
     */
    public void newPlayer(UserData data) {
        saveTask.scheduleNewPlayer(data);
        cache(data);
    }

    /**
     * Used to get the contents of the cache.
     *
     * @return The HashMap containing all Cached UserData
     */
    public HashMap<UUID, UserData> getDataCache() {
        return dataCache;
    }

    /**
     * Used to get the cached commandUse.
     *
     * @return Map with key:value - "/command":4
     */
    public Map<String, Integer> getCommandUse() {
        return commandUse;
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
                    boolean isNewPlayer = !db.wasSeenBefore(uuid);
                    if (isNewPlayer) {
                        newPlayer(player);
                    }
                    startSession(uuid);
                    saveHandlerDataToCache(player);
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

    /**
     * Used to handle a command's execution.
     *
     * @param command "/command"
     */
    public void handleCommand(String command) {
        if (!commandUse.containsKey(command)) {
            commandUse.put(command, 0);
        }
        commandUse.put(command, commandUse.get(command) + 1);
    }
}
