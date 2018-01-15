/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.system.processing.ProcessingQueue;

/**
 * Abstract class for processing different objects using Generics.
 *
 * @author Rsl1122
 */
public abstract class Processor<T> {
    protected final T object;

    public Processor(T object) {
        this.object = object;
    }

    public static void queue(Processor... processors) {
        ProcessingQueue processingQueue = ProcessingQueue.getInstance();
        for (Processor processor : processors) {
            processingQueue.queue(processor);
        }
    }

    public abstract void process();

    public T getObject() {
        return object;
    }

    public void queue() {
        queue(this);
    }
}
