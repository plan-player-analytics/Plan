package com.djrapitops.plan.utilities.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Abstract implementation of a Queue.
 *
 * @param <T> Object this queue consumes
 * @author Rsl1122
 */
public abstract class Queue<T> {

    protected final BlockingQueue<T> queue;
    protected boolean run = true;
    protected Setup<T> setup;

    /**
     * Constructor, defines queue.
     *
     * @param queue BlockingQueue to use for this queue.
     */
    public Queue(BlockingQueue<T> queue) {
        this.queue = queue;
    }

    /**
     * Add a object to the queue, default implementation.
     *
     * @param object Object to add.
     */
    public void add(T object) {
        if (run) {
            queue.add(object);
        }
    }

    /**
     * Used to stop the queue processing and get the unprocessed objects.
     *
     * @return List of unprocessed objects.
     */
    public List<T> stopAndReturnLeftovers() {
        run = false;
        try {
            if (setup != null) {
                setup.stop();
                return new ArrayList<>(queue);
            }
            return new ArrayList<>();
        } finally {
            stop();
        }
    }

    /**
     * Stops all activity and clears the queue.
     */
    public void stop() {
        run = false;
        if (setup != null) {
            setup.stop();
        }
        setup = null;
        queue.clear();
    }

    /**
     * Get how many objects are in the queue.
     *
     * @return size of the queue.
     */
    public int size() {
        return queue.size();
    }
}
