package main.java.com.djrapitops.plan.queue;

import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;

import java.util.concurrent.BlockingQueue;

/**
 * Abstract class representing a queue consumer.
 *
 * @param <T>
 * @author Rsl1122
 */
public abstract class Consumer<T> extends AbsRunnable {

    final BlockingQueue<T> queue;
    boolean run;

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
            Log.error("Consumer interrupted: " + ex.getCause());
            Thread.currentThread().interrupt();
        }
    }

    void stop() {
        run = false;
        super.cancel();
    }

    abstract void clearVariables();

    abstract void consume(T toConsume);
}
