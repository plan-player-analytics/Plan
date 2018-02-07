package com.djrapitops.plan.system.processing;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.utilities.queue.Consumer;
import com.djrapitops.plan.utilities.queue.Queue;
import com.djrapitops.plan.utilities.queue.Setup;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This Class is starts the Process Queue Thread, that processes Processor
 * objects.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class ProcessingQueue extends Queue<Processor> implements SubSystem {

    public ProcessingQueue() {
        super(new ArrayBlockingQueue<>(20000));
        setup = new ProcessSetup(queue);
    }

    public static ProcessingQueue getInstance() {
        ProcessingQueue processingQueue = PlanSystem.getInstance().getProcessingQueue();
        Verify.nullCheck(processingQueue, () -> new IllegalStateException("ProcessingQueue has not been initialized."));
        return processingQueue;
    }

    @Override
    public void enable() {
        setup.go();
    }

    @Override
    public void disable() {
        List<Processor> processors = stopAndReturnLeftovers();
        if (PlanPlugin.getInstance().isReloading()) {
            RunnableFactory.createNew("Re-Add processors", new AbsRunnable() {
                @Override
                public void run() {
                    ProcessingQueue que = ProcessingQueue.getInstance();
                    for (Processor processor : processors) {
                        que.queue(processor);
                    }
                    cancel();
                }
            }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 5L);
        } else {
            Log.info("Processing unprocessed processors. (" + processors.size() + ")");
            for (Processor processor : processors) {
                processor.process();
            }
        }
    }

    /**
     * Used to add Processor object to be processed.
     *
     * @param processor processing object.
     */
    public void queue(Processor processor) {
        if (!queue.offer(processor)) {
            Log.toLog(Processor.class, new IllegalStateException("Processor was not added to Queue"));
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
            String benchName = "Processed " + process.getClass().getSimpleName() + ".";
            Benchmark.start(benchName);
            process.process();
            Benchmark.stop(benchName);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            Log.toLog(process.getClass(), e);
        }
    }
}

class ProcessSetup extends Setup<Processor> {

    ProcessSetup(BlockingQueue<Processor> q) {
        super(
                new ProcessConsumer(q),
                new ProcessConsumer(q),
                new ProcessConsumer(q),
                new ProcessConsumer(q),
                new ProcessConsumer(q),
                new ProcessConsumer(q),
                new ProcessConsumer(q),
                new ProcessConsumer(q)
        );
    }
}
