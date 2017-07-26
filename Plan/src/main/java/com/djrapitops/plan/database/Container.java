package main.java.com.djrapitops.plan.database;

/**
 * Class to contain objects in the batches.
 *
 * @param <T> Object stored.
 * @author Rsl1122
 * @since 3.4.3
 */
public class Container<T> {

    private final T object;
    private final int id;

    /**
     * Constructor for the object.
     *
     * @param object Object to place inside the container.
     * @param id     UserID related to the object.
     */
    public Container(T object, int id) {
        this.object = object;
        this.id = id;
    }

    /**
     * Get the object in the container.
     *
     * @return object.
     */
    public T getObject() {
        return object;
    }

    /**
     * Get the UserID related to the object.
     *
     * @return userID
     */
    public int getId() {
        return id;
    }
}
