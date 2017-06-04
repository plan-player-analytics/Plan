package main.java.com.djrapitops.plan.data.cache.queue;

import java.util.concurrent.BlockingQueue;

/**
 * Abstract class representing a queue consumer.
 *
 * @author Rsl1122
 * @param <T>
 */
public abstract class Consumer<T> implements Runnable {

    boolean run;
    final BlockingQueue<T> queue;

    /**
     * Constructor, defines queue.
     *
     * @param queue Queue to consume from.
     */
    public Consumer(BlockingQueue<T> queue) {
        this.queue = queue;
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

    void stop() {
        run = false;
    }

    abstract void clearVariables();

    abstract void consume(T toConsume);
}
