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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Methods that can be used as functional interfaces when dealing with Maps.
 *
 * @author AuroraLS3
 */
public class Maps {

    private Maps() {
        // Static method class
    }

    public static <V, T, K> Map<V, T> create(K key) {
        return new HashMap<>();
    }

    public static <V, K> Set<V> createSet(K key) {
        return new HashSet<>();
    }

    public static <K, V> Builder<K, V> builder(Class<K> key, Class<V> value) {
        return new Builder<>();
    }

    public static <K, V> Map<V, K> reverse(Map<K, V> map) {
        Map<V, K> reversed = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            reversed.put(entry.getValue(), entry.getKey());
        }
        return reversed;
    }

    public static class Builder<K, V> {
        private final Map<K, V> map;

        private Builder() {
            map = new HashMap<>();
        }

        public Builder<K, V> put(K key, V value) {
            if (value != null) {
                map.put(key, value);
            }
            return this;
        }

        public Map<K, V> build() {
            return map;
        }
    }
}