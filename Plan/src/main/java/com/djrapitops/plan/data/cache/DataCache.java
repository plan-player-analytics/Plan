package main.java.com.djrapitops.plan.data.cache;

import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.player.IPlayer;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.queue.processing.Processor;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

/**
 * This Class contains the Cache.
 * <p>
 * It is used to store command use, active sessions and Unsaved TPS objects
 * objects in memory.
 * <p>
 * Its methods can be used to access all the data it stores and to clear them.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class DataCache extends SessionCache {

    // Plan
    private final Plan plugin;
    private final Database db;

    //Cache
    private Map<String, Integer> commandUse;
    private List<List<TPS>> unsavedTPSHistory;

    // Queues


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
    public DataCache(Plan plugin) {
        super(); // Initializes Session & Location cache.

        this.plugin = plugin;
        db = plugin.getDB();

        commandUse = new HashMap<>();
        if (!getCommandUseFromDb()) {
            Log.error(Locale.get(Msg.ENABLE_DB_FAIL_DISABLE_INFO).toString());
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
     * Used to start the Asynchronous Save Task.
     *
     * @throws IllegalArgumentException BukkitRunnable was given wrong
     *                                  parameters.
     * @throws IllegalStateException    BukkitScheduler is in a wrong state.
     */
    public void startAsyncPeriodicSaveTask() {
        DataCache dataCache = this;
        plugin.getRunnableFactory().createNew(new AbsRunnable("PeriodicCacheSaveTask") {
            private int timesSaved = 0;

            @Override
            public void run() {
                if (periodicTaskIsSaving) {
                    return;
                }
                try {
                    periodicTaskIsSaving = true;
                    Log.debug("Database", "Periodic Cache Save");
                    saveCommandUse();
                    saveUnsavedTPSHistory();
                    timesSaved++;
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + "(" + this.getName() + ")", e);
                } finally {
                    periodicTaskIsSaving = false;
                }
            }
        }).runTaskTimerAsynchronously(60L * 20L * 5, 60L * 20L * 5);
    }

    /**
     * Saves all data in the cache to Database and closes the database down.
     * <p>
     * Stops all tasks.
     * <p>
     * If processingQueue has unprocessed information, it will be processed.
     */
    @Deprecated
    public void saveCacheOnDisable() { // TODO Rewrite
        long time = MiscUtils.getTime();
        Benchmark.start("Cache: SaveOnDisable");
        Benchmark.start("Cache: ProcessOnlineHandlingInfo");
        List<IPlayer> onlinePlayers = plugin.fetch().getOnlinePlayers();
        Log.debug("Online: " + onlinePlayers.size());
        for (IPlayer p : onlinePlayers) {
            UUID uuid = p.getUuid();
            endSession(uuid);
            String worldName = ((Player) p.getWrappedPlayerClass()).getWorld().getName();
        }
//        toProcess.sort(new HandlingInfoTimeComparator());
        Benchmark.stop("Cache: ProcessOnlineHandlingInfo");
        try {
            db.saveCommandUse(commandUse);
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

    private void processUnprocessedHandlingInfo(List<Processor> toProcess) {
        Log.debug("PROCESS: " + toProcess.size());
        for (Processor i : toProcess) {
            i.process();
        }
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
        try {
            Log.debug("Database", "Periodic TPS Save: " + averages.size());
            db.getTpsTable().saveTPSData(averages);
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
        }
    }

    private List<TPS> calculateAverageTpsForEachMinute() {
        final List<TPS> averages = new ArrayList<>();
        if (unsavedTPSHistory.isEmpty()) {
            return new ArrayList<>();
        }
        List<List<TPS>> copy = new ArrayList<>(unsavedTPSHistory);

        for (List<TPS> history : copy) {
            final long lastDate = history.get(history.size() - 1).getDate();
            final double averageTPS = MathUtils.round(MathUtils.averageDouble(history.stream().map(TPS::getTicksPerSecond)));
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
     * Used to get the cached commandUse.
     *
     * @return Map with key:value - "/command":4
     */
    public Map<String, Integer> getCommandUse() {
        return commandUse;
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

    public void addTPSLastMinute(List<TPS> history) {
        // Copy the contents to avoid reference, thus making the whole calculation pointless.
        unsavedTPSHistory.add(new ArrayList<>(history));
    }
}
