package main.java.com.djrapitops.plan.queue;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.queue.processing.Processor;

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
public class DataCacheProcessQueue extends Queue<Processor> {

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
    public void addToPool(Processor info) {
        try {
            queue.add(info);
        } catch (IllegalStateException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}

class ProcessConsumer extends Consumer<Processor> {

    private DataCacheHandler handler;

    ProcessConsumer(BlockingQueue<Processor> q, DataCacheHandler h) {
        super(q, "ProcessQueueConsumer");
        handler = h;
    }

    @Override
    protected void consume(Processor info) {
        if (!Verify.notNull(handler, info)) {
            return;
        }

        DBCallableProcessor p = data -> info.process();

        //TODO handler.getUserDataForProcessing(p, info.getUuid());
    }

    @Override
    protected void clearVariables() {
        if (handler != null) {
            handler = null;
        }
    }
}

class ProcessSetup extends Setup<Processor> {

    ProcessSetup(BlockingQueue<Processor> q, DataCacheHandler h) {
        super(new ProcessConsumer(q, h), new ProcessConsumer(q, h));
    }
}
