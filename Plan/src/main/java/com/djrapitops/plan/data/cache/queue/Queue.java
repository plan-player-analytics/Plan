package main.java.com.djrapitops.plan.data.cache.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Abstract implementation of a Queue.
 *
 * @author Rsl1122
 * @param <T> Object this queue consumes
 */
public abstract class Queue<T> {

    final BlockingQueue<T> queue;
    Setup<T> setup;

    /**
     * Consturctor, defines queue.
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
        queue.add(object);
    }

    /**
     * Used to stop the queue processing & get the unprocessed objects.
     *
     * @return List of unprocessed objects.
     */
    public List<T> stopAndReturnLeftovers() {
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
