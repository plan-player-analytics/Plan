package com.djrapitops.plan.utilities.java;

/**
 * Functional interface that performs an operation that might throw an exception.
 * <p>
 * Follows naming scheme of Java 8 functional interfaces.
 *
 * @author Rsl1122
 */
public interface ThrowingVoidFunction<T extends Throwable> {

    void apply() throws T;

}
