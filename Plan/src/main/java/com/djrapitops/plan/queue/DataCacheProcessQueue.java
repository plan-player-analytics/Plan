package main.java.com.djrapitops.plan.queue;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This Class is starts the Process Queue Thread, that processes HandlingInfo
 * objects.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
// TODO Change Processing Queue to use more generic object as processing.
    // GOAL: Processing queue can be used to process query results from the database
    // & for processing events into statements.
public class DataCacheProcessQueue extends Queue<HandlingInfo> {

    /**
     * Class constructor, starts the new Thread for processing.
     *
     * @param handler current instance of DataCacheHandler.
     */
    public DataCacheProcessQueue(DataCacheHandler handler) {
        super(new ArrayBlockingQueue<>(20000));
        setup = new ProcessSetup(queue, handler);
        setup.go();
    }

    /**
     * Used to add HandlingInfo object to be processed.
     *
     * @param info object that extends HandlingInfo.
     */
    public void addToPool(HandlingInfo info) {
        try {
            queue.add(info);
        } catch (IllegalStateException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * Check whether or not the queue contains a HandlingInfo object with the
     * uuid.
     *
     * @param uuid UUID of the player.
     * @return true/false
     */
    public boolean containsUUID(UUID uuid) {
        return uuid != null && queue.stream().anyMatch(info -> info.getUuid().equals(uuid));
    }
}

class ProcessConsumer extends Consumer<HandlingInfo> {

    private DataCacheHandler handler;

    ProcessConsumer(BlockingQueue<HandlingInfo> q, DataCacheHandler h) {
        super(q, "ProcessQueueConsumer");
        handler = h;
    }

    @Override
    void consume(HandlingInfo info) {
        if (!Verify.notNull(handler, info)) {
            return;
        }

        if (handler.getGetTask().containsUUIDtoBeCached(info.getUuid())) { // Wait for get queue.
            queue.add(info);
            return;
        }

        DBCallableProcessor p = data -> {
            if (!info.process(data)) {
                Log.error("Attempted to process data for wrong uuid: W:" + data.getUuid() + " | R:" + info.getUuid() + " Type:" + info.getType().name());
            }
        };

        handler.getUserDataForProcessing(p, info.getUuid());
    }

    @Override
    void clearVariables() {
        if (handler != null) {
            handler = null;
        }
    }
}

class ProcessSetup extends Setup<HandlingInfo> {

    ProcessSetup(BlockingQueue<HandlingInfo> q, DataCacheHandler h) {
        super(new ProcessConsumer(q, h), new ProcessConsumer(q, h));
    }
}
