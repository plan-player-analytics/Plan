package main.java.com.djrapitops.plan.data.cache.queue;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * This Class contains the Clear Queue Thread, which is clearing data from the DataCache.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class DataCacheClearQueue extends Queue<UUID> {

    /**
     * Class constructor, starts the new Thread for clearing.
     *
     * @param handler current instance of DataCacheHandler.
     */
    public DataCacheClearQueue(DataCacheHandler handler) {
        super(new ArrayBlockingQueue(Settings.PROCESS_CLEAR_LIMIT.getNumber()));
        setup = new ClearSetup(queue, handler);
        setup.go();
    }

    /**
     * Used to schedule UserData to be cleared from the cache.
     *
     * @param uuid UUID of the UserData object (Player's UUID)
     */
    public void scheduleForClear(UUID uuid) {
        Log.debug(uuid + ": Scheduling for clear");
        queue.add(uuid);
    }

    /**
     * Used to schedule multiple UserData objects to be cleared from the cache.
     *
     * @param uuids UUIDs of the UserData object (Players' UUIDs)
     */
    public void scheduleForClear(Collection<UUID> uuids) {
        if (uuids.isEmpty()) {
            return;
        }
        uuids = uuids.stream().filter(Objects::nonNull).collect(Collectors.toList());
        Log.debug("Scheduling for clear: " + uuids);
        try {
            queue.addAll(uuids);
        } catch (IllegalStateException e) {
            Log.error(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Clear Queue", Settings.PROCESS_CLEAR_LIMIT.getNumber()));
        }
    }
}

class ClearConsumer extends Consumer<UUID> implements Runnable {

    private DataCacheHandler handler;

    ClearConsumer(BlockingQueue q, DataCacheHandler handler) {
        super(q, "ClearQueueConsumer");
        this.handler = handler;
    }

    @Override
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

    @Override
    void clearVariables() {
        if (handler != null) {
            handler = null;
        }
    }
}

class ClearSetup extends Setup<UUID> {

    public ClearSetup(BlockingQueue<UUID> q, DataCacheHandler handler) {
        super(new ClearConsumer(q, handler));
    }
}
