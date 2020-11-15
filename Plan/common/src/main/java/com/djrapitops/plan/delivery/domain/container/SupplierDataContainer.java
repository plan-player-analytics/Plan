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
package com.djrapitops.plan.delivery.domain.container;

import com.djrapitops.plan.delivery.domain.keys.Key;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * DataContainer implementation that stores everything in {@link Supplier} objects.
 *
 * @author Rsl1122
 */
public class SupplierDataContainer implements DataContainer {

    private final Map<Key, Supplier> map;
    private final long timeToLive;

    /**
     * Create a SupplierDataContainer with a default TTL of 30 seconds.
     */
    public SupplierDataContainer() {
        this(TimeUnit.SECONDS.toMillis(30L));
    }

    /**
     * Create a SupplierDataContainer with a custom TTL.
     * <p>
     * The old value is not removed from memory until the supplier is called again.
     *
     * @param timeToLive TTL that determines how long a CachingSupplier value is deemed valid.
     */
    public SupplierDataContainer(long timeToLive) {
        this.timeToLive = timeToLive;
        map = new HashMap<>();
    }

    @Override
    public <T> void putRawData(Key<T> key, T obj) {
        putSupplier(key, () -> obj);
    }

    @Override
    public <T> void putSupplier(Key<T> key, Supplier<T> supplier) {
        if (supplier == null) {
            return;
        }
        map.put(key, supplier);
    }

    @Override
    public <T> void putCachingSupplier(Key<T> key, Supplier<T> supplier) {
        if (supplier == null) {
            return;
        }
        map.put(key, new CachingSupplier<>(supplier, timeToLive));
    }

    private <T> Supplier<T> getSupplier(Key<T> key) {
        return map.get(key);
    }

    @Override
    public <T> boolean supports(Key<T> key) {
        return map.containsKey(key);
    }

    @Override
    public <T> Optional<T> getValue(Key<T> key) {
        Supplier<T> supplier = getSupplier(key);
        if (supplier == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(supplier.get());
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> T getUnsafe(Key<T> key) {
        Supplier supplier = map.get(key);
        if (supplier == null) {
            throw new IllegalArgumentException("Unsupported Key: " + key.getKeyName());
        }
        return key.typeCast(supplier.get());
    }

    private void putAll(Map<Key, Supplier> toPut) {
        map.putAll(toPut);
    }

    @Override
    public void putAll(DataContainer dataContainer) {
        if (dataContainer instanceof SupplierDataContainer) {
            putAll(((SupplierDataContainer) dataContainer).map);
        } else {
            for (Map.Entry<Key, Object> entry : dataContainer.getMap().entrySet()) {
                putRawData(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Map<Key, Object> getMap() {
        // Fetches all objects from their Suppliers.
        Map<Key, Object> objectMap = new HashMap<>();
        for (Map.Entry<Key, Supplier> entry : map.entrySet()) {
            objectMap.put(entry.getKey(), entry.getValue().get());
        }
        return objectMap;
    }
}