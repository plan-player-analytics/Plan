package main.java.com.djrapitops.plan.queue;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

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
@Deprecated
public class DataCacheClearQueue extends Queue<UUID> {

    /**
     * Class constructor, starts the new Thread for clearing.
     *
     * @param handler current instance of DataCacheHandler.
     */
    public DataCacheClearQueue(DataCacheHandler handler) {
        super(new ArrayBlockingQueue<>(20000));
        setup = new ClearSetup(queue, handler);
        setup.go();
    }

    /**
     * Used to schedule UserData to be cleared from the cache.
     *
     * @param uuid UUID of the UserData object (Player's UUID)
     */
    public void scheduleForClear(UUID uuid) {
        queue.add(uuid);
    }

    /**
     * Used to schedule multiple UserData objects to be cleared from the cache.
     *
     * @param uuids UUIDs of the UserData object (Players' UUIDs)
     */
    public void scheduleForClear(Collection<UUID> uuids) {
        if (Verify.isEmpty(uuids)) {
            return;
        }
        try {
            queue.addAll(uuids.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        } catch (IllegalStateException e) {
            Log.error(Locale.get(Msg.RUN_WARN_QUEUE_SIZE).parse("Clear Queue", Settings.PROCESS_CLEAR_LIMIT.getNumber()));
        }
    }
}
@Deprecated
class ClearConsumer extends Consumer<UUID> implements Runnable {

    private DataCacheHandler handler;

    ClearConsumer(BlockingQueue<UUID> q, DataCacheHandler handler) {
        super(q, "ClearQueueConsumer");
        this.handler = handler;
    }

    @Override
    void consume(UUID uuid) {
        if (!Verify.notNull(handler, uuid)) {
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
@Deprecated
class ClearSetup extends Setup<UUID> {

    ClearSetup(BlockingQueue<UUID> q, DataCacheHandler handler) {
        super(new ClearConsumer(q, handler));
    }
}
