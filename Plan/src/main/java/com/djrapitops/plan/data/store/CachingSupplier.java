package com.djrapitops.plan.data.store;

import java.util.function.Supplier;

/**
 * Caching layer between Supplier and caller.
 *
 * @author Rsl1122
 */
public class CachingSupplier<T> implements Supplier<T> {

    private final Supplier<T> original;
    private T cachedValue;

    public CachingSupplier(Supplier<T> original) {
        this.original = original;
    }

    @Override
    public T get() {
        if (cachedValue == null) {
            cachedValue = original.get();
        }
        return cachedValue;
    }
}