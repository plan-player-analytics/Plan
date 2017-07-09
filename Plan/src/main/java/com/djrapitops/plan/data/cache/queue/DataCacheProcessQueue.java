package main.java.com.djrapitops.plan.data.cache.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;

/**
 * This Class is starts the Process Queue Thread, that processes HandlingInfo
 * objects.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class DataCacheProcessQueue extends Queue<HandlingInfo> {

    /**
     * Class constructor, starts the new Thread for processing.
     *
     * @param handler current instance of DataCachehandler.
     */
    public DataCacheProcessQueue(DataCacheHandler handler) {
        super(new ArrayBlockingQueue(20000));
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
     * Used to add multiple HandlingInfo objects to be processed.
     *
     * @param info Collection of objects that extends HandlingInfo.
     */
    public void addToPool(Collection<HandlingInfo> info) {
        try {
            queue.addAll(info);
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
        if (uuid == null) {
            return false;
        }
        return new ArrayList<>(queue).stream().anyMatch(info -> info.getUuid().equals(uuid));
    }
}

class ProcessConsumer extends Consumer<HandlingInfo> {

    private DataCacheHandler handler;

    ProcessConsumer(BlockingQueue q, DataCacheHandler h) {
        super(q, "ProcessQueueConsumer");
        handler = h;
    }

    @Override
    void consume(HandlingInfo info) {
        if (handler == null) {
            return;
        }
        if (handler.getGetTask().containsUUIDtoBeCached(info.getUuid())) { // Wait for get queue.
            queue.add(info);
            return;
        }
        Log.debug(info.getUuid() + ": Processing type: " + info.getType().name());
        DBCallableProcessor p = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                if (!info.process(data)) {
                    Log.error("Attempted to process data for wrong uuid: W:" + data.getUuid() + " | R:" + info.getUuid() + " Type:" + info.getType().name());
                }
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
