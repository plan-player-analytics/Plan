package com.djrapitops.plan.data.store;

/**
 * Similar to Google's TypeToken but without requiring whole gson package.
 * <p>
 * Create new instance with {@code new Type<YourObject>() {}}.
 *
 * @author Rsl1122
 */
public abstract class Type<T> {

    public static <K> Type<K> ofClass(Class<K> of) {
        return new Type<K>() {};
    }

    public static <K> Type<K> of(K object) {
        return new Type<K>() {};
    }

}