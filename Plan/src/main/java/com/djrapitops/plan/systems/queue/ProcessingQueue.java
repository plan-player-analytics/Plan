package main.java.com.djrapitops.plan.systems.queue;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.systems.processing.Processor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This Class is starts the Process Queue Thread, that processes Processor
 * objects.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class ProcessingQueue extends Queue<Processor> {

    /**
     * Class constructor, starts the new Thread for processing.
     */
    public ProcessingQueue() {
        super(new ArrayBlockingQueue<>(20000));
        setup = new ProcessSetup(queue);
        setup.go();
    }

    /**
     * Used to add Processor object to be processed.
     *
     * @param processor processing object.
     */
    public void addToQueue(Processor processor) {
        if (!queue.offer(processor)) {
            Log.toLog("ProcessingQueue.addToQueue", new IllegalStateException("Processor was not added to Queue"));
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
        try {
            process.process();
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            Log.toLog(this.getTaskName() + ":" + process.getClass().getSimpleName(), e);
        }
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
