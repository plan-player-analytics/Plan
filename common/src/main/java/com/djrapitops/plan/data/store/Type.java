package com.djrapitops.plan.data.store;

import java.util.Objects;

/**
 * Similar to Google's TypeToken but without requiring whole gson package.
 * <p>
 * Create new instance with {@code new Type<YourObject>() {}}.
 *
 * @author Rsl1122
 */
public abstract class Type<T> {

    private final String genericsSuperClass;

    public Type() {
        genericsSuperClass = getGenericsClass().getGenericSuperclass().getTypeName();
    }

    public static <K> Type<K> ofClass(Class<K> of) {
        return new Type<K>() {
        };
    }

    public static <K> Type<K> of(K object) {
        return new Type<K>() {
        };
    }

    public Class<Type<T>> getGenericsClass() {
        return (Class<Type<T>>) getClass();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Type)) return false;
        Type<?> type = (Type<?>) o;
        return Objects.equals(genericsSuperClass, type.genericsSuperClass);
    }

    @Override
    public int hashCode() {

        return Objects.hash(genericsSuperClass);
    }
}