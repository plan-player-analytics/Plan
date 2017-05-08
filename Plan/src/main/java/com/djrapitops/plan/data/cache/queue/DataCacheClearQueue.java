package main.java.com.djrapitops.plan.data.cache.queue;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;

/**
 *
 * @author Rsl1122
 */
public class DataCacheClearQueue {

    private BlockingQueue<UUID> q;
    private ClearSetup s;

    /**
     *
     * @param plugin
     * @param handler
     */
    public DataCacheClearQueue(Plan plugin, DataCacheHandler handler) {
        q = new ArrayBlockingQueue(Settings.PROCESS_CLEAR_LIMIT.getNumber());
        s = new ClearSetup();
        s.go(q, handler);
    }

    /**
     *
     * @param uuid
     */
    public void scheduleForClear(UUID uuid) {
        Log.debug(uuid+": Scheduling for clear");
        q.add(uuid);
    }

    /**
     *
     * @param uuids
     */
    public void scheduleForClear(Collection<UUID> uuids) {
        if (uuids.isEmpty()) {
            return;
        }
        Log.debug("Scheduling for clear: " + uuids);
        try {
            q.addAll(uuids);
        } catch (IllegalStateException e) {
            Log.error(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Clear Queue", Settings.PROCESS_CLEAR_LIMIT.getNumber() + ""));
        }
    }

    /**
     *
     */
    public void stop() {
        if (s != null) {
            s.stop();
        }
        s = null;
        q.clear();
    }
}

class ClearConsumer implements Runnable {

    private final BlockingQueue<UUID> queue;
    private DataCacheHandler handler;
    private boolean run;

    ClearConsumer(BlockingQueue q, DataCacheHandler handler) {
        queue = q;
        this.handler = handler;
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

    void consume(UUID uuid) {
        if (handler == null) {
            return;
        }
        try {
            if (handler.isDataAccessed(uuid)) {
                queue.add(uuid);
            } else {
                handler.clearFromCache(uuid);
            }
            // if online remove from clear list
        } catch (Exception ex) {
            Log.toLog(this.getClass().getName(), ex);
        }
    }

    void stop() {
        run = false;
        if (handler != null) {
            handler = null;
        }
    }
}

class ClearSetup {

    private ClearConsumer one;

    void go(BlockingQueue<UUID> q, DataCacheHandler handler) {
        one = new ClearConsumer(q, handler);
        new Thread(one).start();
    }

    void stop() {
        one.stop();
    }
}
