package main.java.com.djrapitops.plan.queue;

import main.java.com.djrapitops.plan.Log;
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
public class DataCacheProcessQueue extends Queue<Processor> {

    /**
     * Class constructor, starts the new Thread for processing.
     */
    public DataCacheProcessQueue() {
        super(new ArrayBlockingQueue<>(20000));
        setup = new ProcessSetup(queue);
        setup.go();
    }

    /**
     * Used to add HandlingInfo object to be processed.
     *
     * @param processor object that extends HandlingInfo.
     */
    public void addToQueue(Processor processor) {
        try {
            queue.add(processor);
        } catch (IllegalStateException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}

class ProcessConsumer extends Consumer<Processor> {


    ProcessConsumer(BlockingQueue<Processor> q) {
        super(q, "ProcessQueueConsumer");
    }

    @Override
    protected void consume(Processor process) {
        if (process == null) {
            return;
        }
        process.process();
    }

    @Override
    protected void clearVariables() {
    }
}

class ProcessSetup extends Setup<Processor> {

    ProcessSetup(BlockingQueue<Processor> q) {
        super(new ProcessConsumer(q), new ProcessConsumer(q));
    }
}
