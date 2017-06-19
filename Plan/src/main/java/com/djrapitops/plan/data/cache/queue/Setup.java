package main.java.com.djrapitops.plan.data.cache.queue;

import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import main.java.com.djrapitops.plan.Plan;

/**
 * Abstract representation of a queue setup.
 *
 * @author Rsl1122
 * @param <T> Object this queue consumes.
 */
public abstract class Setup<T> {

    private final Consumer<T>[] consumers;

    /**
     * Constructor, defines consumers.
     *
     * @param consumers Consumers for the new threads.
     */
    public Setup(Consumer<T>... consumers) {
        this.consumers = consumers;
    }

    void go() {
        for (Consumer<T> consumer : consumers) {
            consumer.runTaskAsynchronously();
        }
    }

    void stop() {
        for (Consumer<T> consumer : consumers) {
            consumer.stop();
        }
    }
}
