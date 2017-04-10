package main.java.com.djrapitops.plan.data.cache.queue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.Database;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class DataCacheSaveQueue {

    private BlockingQueue<UserData> q;
    private SaveSetup s;

    /**
     *
     * @param plugin
     */
    public DataCacheSaveQueue(Plan plugin) {
        q = new ArrayBlockingQueue(Settings.PROCESS_SAVE_LIMIT.getNumber());
        s = new SaveSetup();
        s.go(q, plugin.getDB());
    }

    /**
     *
     * @param data
     */
    public void scheduleForSave(UserData data) {
        try {
            q.add(data);
        } catch (IllegalStateException e) {
            getPlugin(Plan.class).logError(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }

    /**
     *
     * @param data
     */
    public void scheduleForSave(Collection<UserData> data) {
        try {
            q.addAll(data);
        } catch (IllegalStateException e) {
            getPlugin(Plan.class).logError(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }

    /**
     *
     * @param data
     */
    public void scheduleNewPlayer(UserData data) {
        try {
            q.add(data);
        } catch (IllegalStateException e) {
            getPlugin(Plan.class).logError(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }
    
    public boolean containsUUID(UUID uuid) {
        return new ArrayList<>(q).stream().map(d -> d.getUuid()).collect(Collectors.toList()).contains(uuid);
    }

    /**
     *
     */
    public void stop() {
        s.stop();
    }
}

class SaveConsumer implements Runnable {

    private final BlockingQueue<UserData> queue;
    private final Database db;
    private boolean run;

    SaveConsumer(BlockingQueue q, Database db) {
        queue = q;
        this.db = db;
        run = true;
    }

    @Override
    public void run() {
        try {
            while (run) {
                consume(queue.take());
            }
        } catch (InterruptedException ex) {
        }
    }

    void consume(UserData data) {
        try {
            db.saveUserData(data.getUuid(), data);
        } catch (SQLException ex) {
            getPlugin(Plan.class).toLog(this.getClass().getName(), ex);
        }
    }

    void stop() {
        run = false;
    }
}

class SaveSetup {

    private SaveConsumer one;
    private SaveConsumer two;

    void go(BlockingQueue<UserData> q, Database db) {
        one = new SaveConsumer(q, db);
        two = new SaveConsumer(q, db);
        new Thread(one).start();
        new Thread(two).start();
    }

    void stop() {
        one.stop();
        two.stop();
    }
}
