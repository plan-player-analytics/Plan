package main.java.com.djrapitops.plan.data.cache.queue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.database.Database;

/**
 * This Class is starts the Get Queue Thread, that fetches data from DataCache.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class DataCacheGetQueue {

    private BlockingQueue<HashMap<UUID, List<DBCallableProcessor>>> q;
    private GetSetup s;

    /**
     * Class constructor, starts the new Thread for fetching.
     *
     * @param plugin current instance of Plan
     */
    public DataCacheGetQueue(Plan plugin) {
        q = new ArrayBlockingQueue(Settings.PROCESS_GET_LIMIT.getNumber());
        s = new GetSetup();
        s.go(q, plugin.getDB());
    }

    /**
     * Schedules UserData objects to be get for the given proecssors.
     *
     * @param uuid UUID of the player whose UserData object is fetched.
     * @param processors Processors which process-method will be called after
     * fetch is complete, with the UserData object.
     */
    public void scheduleForGet(UUID uuid, DBCallableProcessor... processors) {
        Log.debug(uuid + ": Scheduling for get");
        try {
            HashMap<UUID, List<DBCallableProcessor>> map = new HashMap<>();
            if (map.get(uuid) == null) {
                map.put(uuid, new ArrayList<>());
            }
            map.get(uuid).addAll(Arrays.asList(processors));
            q.add(map);
        } catch (IllegalStateException e) {
            Log.error(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Get Queue", Settings.PROCESS_GET_LIMIT.getNumber() + ""));
        }
    }

    /**
     * Stops the activities and clears the queue.
     */
    public void stop() {
        if (s != null) {
            s.stop();
        }
        s = null;
        q.clear();
    }
}

class GetConsumer implements Runnable {

    private final BlockingQueue<HashMap<UUID, List<DBCallableProcessor>>> queue;
    private Database db;
    private boolean run;

    GetConsumer(BlockingQueue q, Database db) {
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

    void consume(HashMap<UUID, List<DBCallableProcessor>> processors) {
        if (db == null) {
            return;
        }
        try {
            for (UUID uuid : processors.keySet()) {
                if (uuid == null) {
                    continue;
                }
                List<DBCallableProcessor> processorsList = processors.get(uuid);
                if (processorsList != null) {
                    Log.debug(uuid + ": Get, For:" + processorsList.size());
                    try {
                        db.giveUserDataToProcessors(uuid, processorsList);
                    } catch (SQLException e) {
                        Log.toLog(this.getClass().getName(), e);
                    }
                }
            }
        } catch (Exception ex) {
            Log.toLog(this.getClass().getName(), ex);
        }
    }

    void stop() {
        run = false;
        if (db != null) {
            db = null;
        }
    }
}

class GetSetup {

    private GetConsumer one;
    private GetConsumer two;

    void go(BlockingQueue<HashMap<UUID, List<DBCallableProcessor>>> q, Database db) {
        one = new GetConsumer(q, db);
        two = new GetConsumer(q, db);
        new Thread(one).start();
        new Thread(two).start();
    }

    void stop() {
        one.stop();
        two.stop();
    }
}
