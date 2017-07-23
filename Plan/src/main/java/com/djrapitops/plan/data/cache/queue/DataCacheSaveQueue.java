package main.java.com.djrapitops.plan.data.cache.queue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.database.Database;

/**
 * This Class is starts the Save Queue Thread, that saves data to the Databse.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class DataCacheSaveQueue extends Queue<UserData> {

    /**
     * Class constructor, starts the new Thread for saving.
     *
     * @param plugin current instance of Plan
     * @param handler DataCacheHandler
     */
    public DataCacheSaveQueue(Plan plugin, DataCacheHandler handler) {
        super(new ArrayBlockingQueue(Settings.PROCESS_SAVE_LIMIT.getNumber()));
        setup = new SaveSetup(queue, handler, plugin.getDB());
        setup.go();
    }

    /**
     * Schedule UserData object to be saved to the database.
     *
     * @param data UserData object.
     */
    public void scheduleForSave(UserData data) {
        Log.debug(data.getUuid() + ": Scheduling for save");
        try {
            queue.add(data);
        } catch (IllegalStateException e) {
            Log.error(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }

    /**
     * Schedule multiple UserData objects to be saved to the database.
     *
     * @param data Collection of UserData objects.
     */
    public void scheduleForSave(Collection<UserData> data) {
        Log.debug("Scheduling for save: " + data.stream().map(UserData::getUuid).collect(Collectors.toList()));
        try {
            queue.addAll(data);
        } catch (IllegalStateException e) {
            Log.error(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }

    /**
     * Schedule UserData object for a new player to be saved to the database.
     *
     * @param data UserData object.
     */
    public void scheduleNewPlayer(UserData data) {
        Log.debug(data.getUuid() + ": Scheduling new Player");
        try {
            queue.add(data);
        } catch (IllegalStateException e) {
            Log.error(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }

    /**
     * Check whether or not the queue contains a UserData object with the uuid.
     *
     * @param uuid UUID of the player.
     * @return true/false
     */
    public boolean containsUUID(UUID uuid) {
        return uuid != null && new ArrayList<>(queue).stream().anyMatch(d -> d.getUuid().equals(uuid));
    }
}

class SaveConsumer extends Consumer<UserData> {

    private Database db;
    private DataCacheHandler handler;

    SaveConsumer(BlockingQueue q, DataCacheHandler handler, Database db) {
        super(q, "SaveQueueConsumer");
        this.db = db;
        this.handler = handler;
        run = true;
    }

    @Override
    void consume(UserData data) {
        if (db == null) {
            return;
        }
        UUID uuid = data.getUuid();
        if (handler.getProcessTask().containsUUID(uuid)) { // Wait for process queue.
            queue.add(data);
            return;
        }
        Log.debug(uuid + ": Saving: " + uuid);
        try {
            db.saveUserData(data);
            data.stopAccessing();
            Log.debug(uuid + ": Saved!");
            if (data.shouldClearAfterSave()) {
                if (handler != null) {
                    handler.getClearTask().scheduleForClear(uuid);
                }
            }
        } catch (SQLException ex) {
//            queue.add(data);
            Log.toLog(this.getClass().getName(), ex);
        }
    }

    @Override
    void clearVariables() {
        if (db != null) {
            db = null;
        }
        if (handler != null) {
            handler = null;
        }
    }
}

class SaveSetup extends Setup<UserData> {

    SaveSetup(BlockingQueue<UserData> q, DataCacheHandler handler, Database db) {
        super(new SaveConsumer(q, handler, db), new SaveConsumer(q, handler, db));
    }
}
