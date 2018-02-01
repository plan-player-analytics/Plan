package com.djrapitops.plan.utilities.queue;

import com.djrapitops.plugin.task.AbsRunnable;

import java.util.concurrent.BlockingQueue;

/**
 * Abstract class representing a queue consumer.
 *
 * @param <T>
 * @author Rsl1122
 */
public abstract class Consumer<T> extends AbsRunnable {

    protected final BlockingQueue<T> queue;
    protected boolean run;

    /**
     * Constructor, defines queue.
     *
     * @param queue Queue to consume from.
     * @param name  Name of the queue.
     */
    public Consumer(BlockingQueue<T> queue, String name) {
        super(name);
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
            Thread.currentThread().interrupt();
        }
    }

    protected void stop() {
        run = false;
        try {
            super.cancel();
        } catch (NullPointerException | IllegalArgumentException ignore) {
            /*ignored*/
        }
    }

    protected abstract void consume(T toConsume);
}
