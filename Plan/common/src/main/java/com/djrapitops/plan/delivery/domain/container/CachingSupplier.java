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

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Caching layer between Supplier and caller.
 * <p>
 * Refreshes the value if 30 seconds have passed since the last call.
 *
 * @author AuroraLS3
 */
public class CachingSupplier<T> implements Supplier<T> {

    private final Supplier<T> original;
    private T cachedValue;
    private long cacheTime;
    private final long timeToLive;

    public CachingSupplier(Supplier<T> original) {
        this(original, TimeUnit.SECONDS.toMillis(30L));
    }

    public CachingSupplier(Supplier<T> original, long timeToLive) {
        this.original = original;
        this.timeToLive = timeToLive;

        cacheTime = 0L;
    }

    @Override
    public T get() {
        if (cachedValue == null || System.currentTimeMillis() - cacheTime > timeToLive) {
            cachedValue = original.get();
            cacheTime = System.currentTimeMillis();
        }
        return cachedValue;
    }

}