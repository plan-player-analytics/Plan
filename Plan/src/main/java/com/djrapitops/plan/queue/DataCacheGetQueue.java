package main.java.com.djrapitops.plan.queue;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This Class is starts the Get Queue Thread, that fetches data from DataCache.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
@Deprecated
public class DataCacheGetQueue extends Queue<Map<UUID, List<DBCallableProcessor>>> {

    /**
     * Class constructor, starts the new Thread for fetching.
     *
     * @param plugin current instance of Plan
     */
    public DataCacheGetQueue(Plan plugin) {
        super(new ArrayBlockingQueue<>(20000));
        setup = new GetSetup(queue, plugin.getDB());
        setup.go();
    }

    /**
     * Schedules UserData objects to be get for the given processors.
     *
     * @param uuid       UUID of the player whose UserData object is fetched.
     * @param processors Processors which process-method will be called after
     *                   fetch is complete, with the UserData object.
     */
    public void scheduleForGet(UUID uuid, DBCallableProcessor... processors) {
        try {
            Map<UUID, List<DBCallableProcessor>> map = new HashMap<>();
            map.put(uuid, Arrays.asList(processors));
            queue.add(map);
        } catch (IllegalStateException e) {
            Log.error(Locale.get(Msg.RUN_WARN_QUEUE_SIZE).parse("Get Queue", 20000));
        }
    }

    @Deprecated
    class GetConsumer extends Consumer<Map<UUID, List<DBCallableProcessor>>> {

        private Database db;

        GetConsumer(BlockingQueue<Map<UUID, List<DBCallableProcessor>>> q, Database db) {
            super(q, "GetQueueConsumer");
            this.db = db;
        }

        @Override
        protected void consume(Map<UUID, List<DBCallableProcessor>> processors) {
            if (!Verify.notNull(processors, db)) {
                return;
            }

            try {
                for (Map.Entry<UUID, List<DBCallableProcessor>> entrySet : processors.entrySet()) {
                    UUID uuid = entrySet.getKey();
                    List<DBCallableProcessor> processorsList = entrySet.getValue();
                    if (uuid == null || Verify.isEmpty(processorsList)) {
                        continue;
                    }
                    Log.debug("Database", uuid + ": Get, For:" + processorsList.size());
                    db.giveUserDataToProcessors(uuid, processorsList);
                }
            } catch (Exception ex) {
                Log.toLog(this.getClass().getName(), ex);
            }
        }

        @Override
        protected void clearVariables() {
            if (db != null) {
                db = null;
            }
        }
    }

    @Deprecated
    class GetSetup extends Setup<Map<UUID, List<DBCallableProcessor>>> {

        GetSetup(BlockingQueue<Map<UUID, List<DBCallableProcessor>>> q, Database db) {
            super(new GetConsumer(q, db), new GetConsumer(q, db));
        }
    }
}
