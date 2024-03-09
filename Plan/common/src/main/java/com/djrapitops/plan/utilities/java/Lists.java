/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.java;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Methods that can be used as functional interfaces when dealing with Maps.
 *
 * @author AuroraLS3
 */
public class Lists {

    private Lists() {
        // Static method class
    }

    public static <V, K> List<V> create(K key) {
        return new ArrayList<>();
    }

    public static <V> Lists.Builder<V> builder(Class<V> ofType) {
        return new Lists.Builder<>();
    }

    /**
     * Efficient replacement for List#stream().filter(keep).collect(Collectors.toList()).
     *
     * @param original Original list
     * @param keep     Condition for keeping on the list
     * @param <T>      Type of the list objects
     * @param <V>      Supertype for T if exists, T if not
     * @return List with elements in original that keep returned true for.
     */
    public static <T extends V, V> List<T> filter(Collection<T> original, Predicate<V> keep) {
        List<T> filtered = new ArrayList<>();
        for (T value : original) {
            if (keep.test(value)) filtered.add(value);
        }
        return filtered;
    }

    /**
     * Efficient replacement for List#stream().map(mapper).collect(Collectors.toList()).
     *
     * @param original Original list
     * @param mapper   Function to change object of type A to type B
     * @param <A>      Type of the old list objects
     * @param <B>      Type of the new list objects
     * @return List with elements in original that keep returned true for.
     */
    public static <A, B> List<B> map(Collection<A> original, Function<A, B> mapper) {
        List<B> mapped = new ArrayList<>();
        for (A element : original) {
            mapped.add(mapper.apply(element));
        }
        return mapped;
    }

    /**
     * Efficient replacement for List#stream().map(mapper).collect(Collectors.toSet()).
     *
     * @param original Original list
     * @param mapper   Function to change object of type A to type B
     * @param <A>      Type of the old list objects
     * @param <B>      Type of the new list objects
     * @return Set with elements in original that keep returned true for.
     */
    public static <A, B> Set<B> mapUnique(Collection<A> original, Function<A, B> mapper) {
        Set<B> mapped = new HashSet<>();
        for (A element : original) {
            mapped.add(mapper.apply(element));
        }
        return mapped;
    }

    public static class Builder<V> {
        private final List<V> list;

        private Builder() {
            list = new ArrayList<>();
        }

        public Lists.Builder<V> add(V value) {
            list.add(value);
            return this;
        }

        public Lists.Builder<V> addAll(Collection<V> values) {
            list.addAll(values);
            return this;
        }

        public Lists.Builder<V> addAll(V[] values) {
            return addAll(Arrays.asList(values));
        }

        public Lists.Builder<V> apply(UnaryOperator<Lists.Builder<V>> operator) {
            return operator.apply(this);
        }

        public List<V> build() {
            return list;
        }
    }
}