package main.java.com.djrapitops.plan.data.cache;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import main.java.com.djrapitops.plan.Plan;
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

    public DataCacheSaveQueue(Plan plugin) {
        q = new ArrayBlockingQueue(1000);
        s = new SaveSetup();
        s.go(q, plugin.getDB());
    }

    public void scheduleForSave(UserData data) {
        q.add(data);
    }

    public void scheduleForSave(Collection<UserData> data) {
        q.addAll(data);
    }

    public void scheduleNewPlayer(UserData data) {
        q.add(data);
    }
    
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
