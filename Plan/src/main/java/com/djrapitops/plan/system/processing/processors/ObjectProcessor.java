/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.system.processing.Processor;

/**
 * Abstract class for processing different objects using Generics.
 *
 * @author Rsl1122
 */
public abstract class ObjectProcessor<T> implements Processor {
    protected final T object;

    public ObjectProcessor(T object) {
        this.object = object;
    }

    @Override
    public abstract void process();

    protected T getObject() {
        return object;
    }

    public void queue() {
        Processor.queue(this);
    }
}
