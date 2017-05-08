package main.java.com.djrapitops.plan.data.cache.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;

/**
 *
 * @author Rsl1122
 */
public class DataCacheProcessQueue {

    private BlockingQueue<HandlingInfo> queue;
    private ProcessSetup setup;

    /**
     *
     * @param handler
     */
    public DataCacheProcessQueue(DataCacheHandler handler) {
        queue = new ArrayBlockingQueue(20000);
        setup = new ProcessSetup();
        setup.go(queue, handler);
    }

    /**
     *
     * @param info
     */
    public void addToPool(HandlingInfo info) {
        try {
            queue.add(info);
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
            queue.addAll(info);
        } catch (IllegalStateException e) {
//            getPlugin(Plan.class).logError(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Save Queue", Settings.PROCESS_SAVE_LIMIT.getNumber() + ""));
        }
    }

    /**
     *
     * @param uuid
     * @return
     */
    public boolean containsUUID(UUID uuid) {
        return new ArrayList<>(queue).stream().map(d -> d.getUuid()).collect(Collectors.toList()).contains(uuid);
    }

    /**
     *
     * @return
     */
    public List<HandlingInfo> stop() {
        try {
            if (setup != null) {
                setup.stop();
                return new ArrayList<>(queue);
            }
            return new ArrayList<>();
        } finally {
            setup = null;
            queue.clear();
        }
    }
}

class ProcessConsumer implements Runnable {

    private final BlockingQueue<HandlingInfo> queue;
    private DataCacheHandler handler;
    private boolean run;

    ProcessConsumer(BlockingQueue q, DataCacheHandler h) {
        handler = h;
        queue = q;
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
        if (handler == null) {
            return;
        }
        Log.debug(info.getUuid()+": Processing type: " + info.getType().name());
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

    void stop() {
        run = false;
        if (handler != null) {
            handler = null;
        }
    }
}

class ProcessSetup {

    private ProcessConsumer one;
    private ProcessConsumer two;

    void go(BlockingQueue<HandlingInfo> q, DataCacheHandler h) {
        one = new ProcessConsumer(q, h);
        two = new ProcessConsumer(q, h);
        new Thread(one).start();
        new Thread(two).start();
    }

    void stop() {
        one.stop();
        two.stop();
    }
}
