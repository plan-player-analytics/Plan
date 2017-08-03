package main.java.com.djrapitops.plan.data.cache;

import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.player.IPlayer;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheClearQueue;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheGetQueue;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheProcessQueue;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheSaveQueue;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.LogoutInfo;
import main.java.com.djrapitops.plan.data.handling.info.ReloadInfo;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.comparators.HandlingInfoTimeComparator;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

/**
 * This Class contains the Cache.
 * <p>
 * This class is the main processing class that initialises Save, Clear, Process
 * and Get queue and Starts the asynchronous save task.
 * <p>
 * It is used to store command use, locations, active sessions and UserData
 * objects in memory.
 * <p>
 * Its methods can be used to access all the data it stores and to clear them.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class DataCacheHandler extends SessionCache {

    // Cache
    private final Map<UUID, UserData> dataCache;

    // Plan
    private final Plan plugin;
    private final Database db;

    //Cache
    private Map<String, Integer> commandUse;
    private List<List<TPS>> unsavedTPSHistory;

    // Queues
    private DataCacheSaveQueue saveTask;
    private DataCacheClearQueue clearTask;
    private DataCacheProcessQueue processTask;
    private DataCacheGetQueue getTask;

    // Variables
    private boolean periodicTaskIsSaving = false;

    /**
     * Class Constructor.
     * <p>
     * Gets the Database from the plugin. Starts the queues. Registers
     * Asynchronous Periodic Save Task
     *
     * @param plugin Current instance of Plan
     */
    public DataCacheHandler(Plan plugin) {
        super(); // Initializes Session & Location cache.

        this.plugin = plugin;
        db = plugin.getDB();

        dataCache = new HashMap<>();
        startQueues();

        commandUse = new HashMap<>();
        if (!getCommandUseFromDb()) {
            Log.error(Phrase.DB_FAILURE_DISABLE + "");
            plugin.disablePlugin();
            return;
        }
        unsavedTPSHistory = new ArrayList<>();
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
        getTask = new DataCacheGetQueue(plugin);
        processTask = new DataCacheProcessQueue(this);
        clearTask = new DataCacheClearQueue(this);
        saveTask = new DataCacheSaveQueue(plugin, this);
    }

    /**
     * Used to start the Asynchronous Save Task.
     *
     * @throws IllegalArgumentException BukkitRunnable was given wrong
     *                                  parameters.
     * @throws IllegalStateException    BukkitScheduler is in a wrong state.
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
        DataCacheHandler handler = this;
        plugin.getRunnableFactory().createNew(new AbsRunnable("PeriodicCacheSaveTask") {
            private int timesSaved = 0;

            @Override
            public void run() {
                if (periodicTaskIsSaving) {
                    return;
                }
                try {
                    periodicTaskIsSaving = true;
                    handler.saveHandlerDataToCache();
                    handler.saveCachedUserData();
                    if (timesSaved % clearAfterXsaves == 0) {
                        handler.clearCache();
                    }
                    saveCommandUse();
                    saveUnsavedTPSHistory();
                    timesSaved++;
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + "(" + this.getName() + ")", e);
                } finally {
                    periodicTaskIsSaving = false;
                }
            }
        }).runTaskTimerAsynchronously(60L * 20L * minutes, 60L * 20L * minutes);
    }

    /**
     * Uses Database or Cache to retrieve the UserData of a matching player.
     * <p>
     * Caches the data to the Cache if cache-parameter is true.
     *
     * @param processor DBCallableProcessor Object used to process the data
     *                  after it was retrieved
     * @param uuid      Player's UUID
     * @param cache     Whether or not the UserData will be Cached in this instance
     *                  of DataCacheHandler after it has been fetched (if not already fetched)
     */
    public void getUserDataForProcessing(DBCallableProcessor processor, UUID uuid, boolean cache) {
        Log.debug(uuid + ": HANDLER getForProcess," + " Cache:" + cache);
        UserData uData = dataCache.get(uuid);
        if (uData == null) {
            if (cache) {
                DBCallableProcessor cacher = this::cache;
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
     * <p>
     * If a object already exists it will be replaced.
     *
     * @param data UserData object with the UUID inside used as key.
     */
    public void cache(UserData data) {
        data.setOnline(true);
        dataCache.put(data.getUuid(), data);
        Log.debug(Phrase.CACHE_ADD.parse(data.getUuid().toString()));
    }

    /**
     * Uses Database or Cache to retrieve the UserData of a matching player.
     * <p>
     * Always Caches the data after retrieval (unless already cached)
     *
     * @param processor DBCallableProcessor Object used to process the data
     *                  after it was retrieved
     * @param uuid      Player's UUID
     */
    public void getUserDataForProcessing(DBCallableProcessor processor, UUID uuid) {
        getUserDataForProcessing(processor, uuid, true);
    }

    /**
     * Saves all UserData in the cache to Database.
     * <p>
     * Should only be called from Async thread
     */
    public void saveCachedUserData() {
        Set<UserData> data = new HashSet<>();
        data.addAll(dataCache.values());
        try {
            db.saveMultipleUserData(data);
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
        }
    }

    /**
     * Used to add event HandlingInfo to the processTask's pool.
     * <p>
     * Given HandlingInfo object's process method will be called.
     *
     * @param i Object that extends HandlingInfo.
     */
    public void addToPool(HandlingInfo i) {
        if (i == null) {
            return;
        }
        Log.debug(i.getUuid() + ": Adding to pool, type:" + i.getType().name());
        processTask.addToPool(i);
    }

    /**
     * Saves all data in the cache to Database and closes the database down.
     * <p>
     * Stops all tasks.
     * <p>
     * If processTask has unprocessed information, it will be processed.
     */
    public void saveCacheOnDisable() {
        long time = MiscUtils.getTime();
        Benchmark.start("Cache: SaveOnDisable");
        saveTask.stop();
        getTask.stop();
        clearTask.stop();
        List<HandlingInfo> toProcess = processTask.stopAndReturnLeftovers();
        Benchmark.start("Cache: ProcessOnlineHandlingInfo");
        Log.debug("ToProcess size: " + toProcess.size() + " DataCache size: " + dataCache.keySet().size());
        List<IPlayer> onlinePlayers = plugin.fetch().getOnlinePlayers();
        Log.debug("Online: " + onlinePlayers.size());
        for (IPlayer p : onlinePlayers) {
            UUID uuid = p.getUuid();
            endSession(uuid);
            String worldName = ((Player) p.getWrappedPlayerClass()).getWorld().getName();
            toProcess.add(new LogoutInfo(uuid, time, p.isBanned(), p.getGamemode().name(), getSession(uuid), worldName));
        }
        Log.debug("ToProcess size_AFTER: " + toProcess.size() + " DataCache size: " + dataCache.keySet().size());
        toProcess.sort(new HandlingInfoTimeComparator());
        processUnprocessedHandlingInfo(toProcess);
        Benchmark.stop("Cache: ProcessOnlineHandlingInfo");
        List<UserData> data = new ArrayList<>();
        data.addAll(dataCache.values());
        Log.debug("SAVING, DataCache size: " + dataCache.keySet().size());
        try {
            db.saveCommandUse(commandUse);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        try {
            db.saveMultipleUserData(data);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        saveUnsavedTPSHistory();
        try {
            db.close();
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        Benchmark.stop("Cache: SaveOnDisable");
    }

    private void processUnprocessedHandlingInfo(List<HandlingInfo> toProcess) {
        Log.debug("PROCESS: " + toProcess.size());
        for (HandlingInfo i : toProcess) {
            UserData uData = dataCache.get(i.getUuid());
            if (uData == null) {
                DBCallableProcessor p = i::process;
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
        DBCallableProcessor saveProcessor = data -> {
            data.access();
            data.setClearAfterSave(true);
            saveTask.scheduleForSave(data);
        };
        getUserDataForProcessing(saveProcessor, uuid);
    }

    /**
     * Saves the cached CommandUse.
     * <p>
     * Should be only called from an Asynchronous Thread.
     */
    public void saveCommandUse() {
        try {
            db.saveCommandUse(new HashMap<>(commandUse));
        } catch (SQLException | NullPointerException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    public void saveUnsavedTPSHistory() {
        List<TPS> averages = calculateAverageTpsForEachMinute();
        if (averages.isEmpty()) {
            return;
        }

        db.getTpsTable().saveTPSData(averages);
    }

    private List<TPS> calculateAverageTpsForEachMinute() {
        final List<TPS> averages = new ArrayList<>();
        if (unsavedTPSHistory.isEmpty()) {
            return new ArrayList<>();
        }
        List<List<TPS>> copy = new ArrayList<>(unsavedTPSHistory);

        for (List<TPS> history : copy) {
            final long lastDate = history.get(history.size() - 1).getDate();
            final double averageTPS = MathUtils.round(MathUtils.averageDouble(history.stream().map(TPS::getTps)));
            final int averagePlayersOnline = (int) MathUtils.averageInt(history.stream().map(TPS::getPlayers));
            final double averageCPUUsage = MathUtils.round(MathUtils.averageDouble(history.stream().map(TPS::getCPUUsage)));
            final long averageUsedMemory = MathUtils.averageLong(history.stream().map(TPS::getUsedMemory));
            final int averageEntityCount = (int) MathUtils.averageInt(history.stream().map(TPS::getEntityCount));
            final int averageChunksLoaded = (int) MathUtils.averageInt(history.stream().map(TPS::getChunksLoaded));

            averages.add(new TPS(lastDate, averageTPS, averagePlayersOnline, averageCPUUsage, averageUsedMemory, averageEntityCount, averageChunksLoaded));
        }
        unsavedTPSHistory.removeAll(copy);
        return averages;
    }

    /**
     * Refreshes the calculations for all online players with ReloadInfo.
     */
    public void saveHandlerDataToCache() {
        final List<IPlayer> onlinePlayers = plugin.fetch().getOnlinePlayers();
        onlinePlayers.forEach(p -> saveHandlerDataToCache(p, false));
    }

    private void saveHandlerDataToCache(IPlayer p, boolean pool) {
        long time = MiscUtils.getTime();
        UUID uuid = p.getUuid();
        String worldName = ((Player) p.getWrappedPlayerClass()).getWorld().getName();
        ReloadInfo info = new ReloadInfo(uuid, time, p.getAddress().getAddress(), p.isBanned(), p.getDisplayName(), p.getGamemode().name(), worldName);
        if (!pool) {
            UserData data = dataCache.get(uuid);
            if (data != null) {
                info.process(data);
                return;
            }
        }
        addToPool(info);
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
        if (plugin.fetch().isOnline(uuid)) {
            Log.debug(uuid + ": Online, did not clear");
            UserData data = dataCache.get(uuid);
            if (data != null) {
                data.setClearAfterSave(false);
            }
        } else {
            dataCache.remove(uuid);
            Log.debug(Phrase.CACHE_REMOVE.parse(uuid.toString()));
        }
    }

    /**
     * Schedules a matching UserData object to be cleared from the cache.
     *
     * @param uuid Player's UUID.
     */
    public void scheduldeForClear(UUID uuid) {
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
    public void newPlayer(IPlayer player) {
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
     * @return The Map containing all Cached UserData
     */
    public Map<UUID, UserData> getDataCache() {
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
     * <p>
     * Calls all the methods that are ran when PlayerJoinEvent is fired
     */
    public void handleReload() {
        plugin.getRunnableFactory().createNew(new AbsRunnable("ReloadCacheUpdateTask") {
            @Override
            public void run() {
                final List<IPlayer> onlinePlayers = plugin.fetch().getOnlinePlayers();
                for (IPlayer player : onlinePlayers) {
                    UUID uuid = player.getUuid();
                    boolean isNewPlayer = !db.wasSeenBefore(uuid);
                    if (isNewPlayer) {
                        newPlayer(player);
                    }
                    startSession(uuid);
                    saveHandlerDataToCache(player, true);
                }
                this.cancel();
            }
        }).runTaskAsynchronously();
    }

    /**
     * Used to handle a command's execution.
     *
     * @param command "/command"
     */
    public void handleCommand(String command) {
        int amount = commandUse.getOrDefault(command, 0);

        commandUse.put(command, amount + 1);
    }

    /**
     * @return
     */
    public DataCacheSaveQueue getSaveTask() {
        return saveTask;
    }

    /**
     * @return
     */
    public DataCacheClearQueue getClearTask() {
        return clearTask;
    }

    /**
     * @return
     */
    public DataCacheProcessQueue getProcessTask() {
        return processTask;
    }

    /**
     * @return
     */
    public DataCacheGetQueue getGetTask() {
        return getTask;
    }

    public void addTPSLastMinute(List<TPS> history) {
        // Copy the contents to avoid reference, thus making the whole calculation pointless.
        unsavedTPSHistory.add(new ArrayList<>(history));
    }
}
