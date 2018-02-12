/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing;

/**
 * Interface for ProcessingQueue.
 * <p>
 * Allows lambda Processor creation.
 *
 * @author Rsl1122
 */
public interface Processor {

    static void queueMany(Processor... processors) {
        ProcessingQueue processingQueue = ProcessingQueue.getInstance();
        for (Processor processor : processors) {
            processingQueue.queue(processor);
        }
    }

    /**
     * A way to run code Async in ProcessingQueue.
     * <p>
     * Good for lambdas.
     *
     * @param processor Processor.
     */
    static void queue(Processor processor) {
        ProcessingQueue.getInstance().queue(processor);
    }

    void process();

}