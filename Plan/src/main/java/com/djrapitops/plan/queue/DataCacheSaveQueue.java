package main.java.com.djrapitops.plan.queue;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This Class is starts the Save Queue Thread, that saves data to the Database.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
@Deprecated
public class DataCacheSaveQueue extends Queue<UserData> {

    /**
     * Class constructor, starts the new Thread for saving.
     *
     * @param plugin  current instance of Plan
     * @param handler DataCacheHandler
     */
    public DataCacheSaveQueue(Plan plugin, DataCacheHandler handler) {
        super(new ArrayBlockingQueue<>(20000));
        setup = new SaveSetup(queue, handler, plugin.getDB());
        setup.go();
    }

    /**
     * Schedule UserData object to be saved to the database.
     *
     * @param data UserData object.
     */
    public void scheduleForSave(UserData data) {
        try {
            queue.add(data);
        } catch (IllegalStateException e) {
            Log.error(Locale.get(Msg.RUN_WARN_QUEUE_SIZE).parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber()));
        }
    }

    /**
     * Schedule UserData object for a new player to be saved to the database.
     *
     * @param data UserData object.
     */
    public void scheduleNewPlayer(UserData data) {
        Log.debug(data.getUuid() + ": Scheduling new Player");
        scheduleForSave(data);
    }

    /**
     * Check whether or not the queue contains a UserData object with the uuid.
     *
     * @param uuid UUID of the player.
     * @return true/false
     */
    public boolean containsUUID(UUID uuid) {
        return uuid != null && queue.stream().anyMatch(d -> d.getUuid().equals(uuid));
    }
}
@Deprecated
class SaveConsumer extends Consumer<UserData> {

    private Database db;
    private DataCacheHandler handler;

    SaveConsumer(BlockingQueue<UserData> q, DataCacheHandler handler, Database db) {
        super(q, "SaveQueueConsumer");
        this.db = db;
        this.handler = handler;
        run = true;
    }

    @Override
    void consume(UserData data) {
        if (!Verify.notNull(handler, db, data)) {
            return;
        }

        UUID uuid = data.getUuid();
        if (handler.getProcessTask().containsUUID(uuid)) { // Wait for process queue.
            queue.add(data);
            return;
        }

        try {
            db.saveUserData(data);
            data.stopAccessing();
            if (data.shouldClearAfterSave()) {
                handler.getClearTask().scheduleForClear(uuid);
            }
        } catch (SQLException ex) {
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
@Deprecated
class SaveSetup extends Setup<UserData> {

    SaveSetup(BlockingQueue<UserData> q, DataCacheHandler handler, Database db) {
        super(new SaveConsumer(q, handler, db), new SaveConsumer(q, handler, db));
    }
}
