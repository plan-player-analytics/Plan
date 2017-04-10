package main.java.com.djrapitops.plan.data.cache.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.database.Database;

/**
 *
 * @author Rsl1122
 */
public class DataCacheProcessQueue {

    private BlockingQueue<HandlingInfo> q;
    private DataCacheHandler h;
    private ProcessSetup s;

    /**
     *
     * @param plugin
     * @param handler
     */
    public DataCacheProcessQueue(Plan plugin, DataCacheHandler handler) {
        h = handler;
        q = new ArrayBlockingQueue(20000);
        s = new ProcessSetup();
        s.go(q, plugin.getDB(), h);
    }

    /**
     *
     * @param info
     */
    public void addToPool(HandlingInfo info) {
        try {
            q.add(info);
        } catch (IllegalStateException e) {
//            getPlugin(Plan.class).logError(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }

    /**
     *
     * @param info
     */
    public void addToPool(Collection<HandlingInfo> info) {
        try {
            q.addAll(info);
        } catch (IllegalStateException e) {
//            getPlugin(Plan.class).logError(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }
    
    public boolean containsUUID(UUID uuid) {
        return new ArrayList<>(q).stream().map(d -> d.getUuid()).collect(Collectors.toList()).contains(uuid);
    }

    /**
     *
     * @return 
     */
    public List<HandlingInfo> stop() {
        return s.stop();
    }
}

class ProcessConsumer implements Runnable {

    private final BlockingQueue<HandlingInfo> queue;
    private final DataCacheHandler handler;
    private final Database db;
    private boolean run;

    ProcessConsumer(BlockingQueue q, Database db, DataCacheHandler h) {
        handler = h;
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

    void consume(HandlingInfo info) {
        DBCallableProcessor p = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                info.process(data);
            }
        };
        handler.getUserDataForProcessing(p, info.getUuid());
    }

    Collection<HandlingInfo> stop() {
        run = false;
        return queue;        
    }
}

class ProcessSetup {

    private ProcessConsumer one;
    private ProcessConsumer two;

    void go(BlockingQueue<HandlingInfo> q, Database db, DataCacheHandler h) {
        one = new ProcessConsumer(q, db, h);
        two = new ProcessConsumer(q, db, h);
        new Thread(one).start();
        new Thread(two).start();
    }

    List<HandlingInfo> stop() {        
        List<HandlingInfo> i = new ArrayList<>(one.stop());
        i.addAll(two.stop());
        return i;
    }
}
