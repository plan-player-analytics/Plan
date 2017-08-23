package main.java.com.djrapitops.plan.data.cache;

import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

    private final Database db;

    //Cache
    private Map<String, Integer> commandUse;

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
        super(plugin); // Initializes Session & Location cache.
        db = plugin.getDB();

        commandUse = new HashMap<>();
        if (!getCommandUseFromDb()) {
            Log.error(Locale.get(Msg.ENABLE_DB_FAIL_DISABLE_INFO).toString());
            plugin.disablePlugin();
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
}
