/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing;

/**
 * Abstract class for processing different objects using Generics.
 *
 * @author Rsl1122
 */
public abstract class Processor<T> {
    protected T object;

    public Processor(T object) {
        this.object = object;
    }

    public abstract void process();

    public T getObject() {
        return object;
    }
}
