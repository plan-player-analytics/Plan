package main.java.com.djrapitops.plan.systems.queue;

import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.PlanBungee;

/**
 * Abstract representation of a queue setup.
 *
 * @param <T> Object this queue consumes.
 * @author Rsl1122
 */
public abstract class Setup<T> {

    private final Consumer<T>[] consumers;

    /**
     * Constructor, defines consumers.
     *
     * @param consumers Consumers for the new threads.
     */
    @SafeVarargs
    public Setup(Consumer<T>... consumers) {
        this.consumers = consumers;
    }

    public void go() {
        for (Consumer<T> consumer : consumers) {
            getRunnableFactory().createNew(consumer).runTaskAsynchronously();
        }
    }

    public void stop() {
        for (Consumer<T> consumer : consumers) {
            consumer.stop();
        }
    }

    private RunnableFactory getRunnableFactory() {
        if (Compatibility.isBukkitAvailable()) {
            return Plan.getInstance().getRunnableFactory();
        } else {
            return PlanBungee.getInstance().getRunnableFactory();
        }
    }
}
