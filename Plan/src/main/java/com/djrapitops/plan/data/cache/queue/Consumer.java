package main.java.com.djrapitops.plan.data.cache.queue;

import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import java.util.concurrent.BlockingQueue;
import main.java.com.djrapitops.plan.Plan;

/**
 * Abstract class representing a queue consumer.
 *
 * @author Rsl1122
 * @param <T>
 */
public abstract class Consumer<T> extends RslBukkitRunnable<Plan> {

    boolean run;
    final BlockingQueue<T> queue;

    /**
     * Constructor, defines queue.
     *
     * @param queue Queue to consume from.
     * @param name Name of the queue.
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
        }
    }

    void stop() {
        run = false;
        super.cancel();
    }

    abstract void clearVariables();

    abstract void consume(T toConsume);
}
