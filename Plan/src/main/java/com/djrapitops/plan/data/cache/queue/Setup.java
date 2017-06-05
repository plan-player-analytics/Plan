package main.java.com.djrapitops.plan.data.cache.queue;

/**
 * Abstract representation of a queue setup.
 *
 * @author Rsl1122
 * @param <T> Object this queue consumes.
 */
public abstract class Setup<T> {

    private Consumer<T>[] consumers;

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
            new Thread(consumer).start();
        }
    }

    void stop() {
        for (Consumer<T> consumer : consumers) {
            consumer.stop();
        }
    }
}
