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

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * DataContainer implementation that delegates the method calls to other DataContainer implementations.
 *
 * @author AuroraLS3
 */
public class DynamicDataContainer implements DataContainer {

    private final SupplierDataContainer supplierDataContainer;
    private final RawDataContainer rawDataContainer;

    public DynamicDataContainer() {
        supplierDataContainer = new SupplierDataContainer();
        rawDataContainer = new RawDataContainer();
    }

    @Override
    public <T> void putRawData(Key<T> key, T obj) {
        rawDataContainer.putRawData(key, obj);
    }

    @Override
    public <T> void putSupplier(Key<T> key, Supplier<T> supplier) {
        supplierDataContainer.putSupplier(key, supplier);
    }

    @Override
    public <T> void putCachingSupplier(Key<T> key, Supplier<T> supplier) {
        supplierDataContainer.putCachingSupplier(key, supplier);
    }

    @Override
    public <T> boolean supports(Key<T> key) {
        return rawDataContainer.supports(key) || supplierDataContainer.supports(key);
    }

    @Override
    public <T> Optional<T> getValue(Key<T> key) {
        Optional<T> raw = rawDataContainer.getValue(key);
        if (raw.isPresent()) {
            return raw;
        } else {
            return supplierDataContainer.getValue(key);
        }
    }

    @Override
    public <T> T getUnsafe(Key<T> key) {
        if (rawDataContainer.supports(key)) {
            return rawDataContainer.getUnsafe(key);
        } else {
            return supplierDataContainer.getUnsafe(key);
        }
    }

    @Override
    public void putAll(DataContainer dataContainer) {
        if (dataContainer instanceof SupplierDataContainer) {
            supplierDataContainer.putAll(dataContainer);
        } else if (dataContainer instanceof RawDataContainer) {
            rawDataContainer.putAll(dataContainer);
        } else {
            rawDataContainer.putAll(dataContainer.getMap());
        }
    }

    @Override
    public void clear() {
        rawDataContainer.clear();
        supplierDataContainer.clear();
    }

    @Override
    public Map<Key, Object> getMap() {
        Map<Key, Object> map = supplierDataContainer.getMap();
        map.putAll(rawDataContainer.getMap());
        return map;
    }
}