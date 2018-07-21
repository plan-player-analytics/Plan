package com.djrapitops.plan.data.store;

import com.djrapitops.plugin.api.TimeAmount;

import java.util.function.Supplier;

/**
 * Caching layer between Supplier and caller.
 * <p>
 * Refreshes the value if 30 seconds have passed since the last call.
 *
 * @author Rsl1122
 */
public class CachingSupplier<T> implements Supplier<T> {

    private final Supplier<T> original;
    private T cachedValue;
    private long cacheTime;

    public CachingSupplier(Supplier<T> original) {
        this.original = original;
        cacheTime = 0L;
    }

    @Override
    public T get() {
        if (cachedValue == null || System.currentTimeMillis() - cacheTime > TimeAmount.SECOND.ms() * 30L) {
            cachedValue = original.get();
            cacheTime = System.currentTimeMillis();
        }
        return cachedValue;
    }
}