/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.queue.processing;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public interface Processor<T> {
    public void process(T object);
}
