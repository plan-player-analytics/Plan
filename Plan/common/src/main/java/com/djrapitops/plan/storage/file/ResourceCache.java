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
package com.djrapitops.plan.storage.file;

import com.djrapitops.plan.utilities.dev.Untrusted;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * In-memory cache for different resources on disk or jar.
 *
 * @author AuroraLS3
 */
public class ResourceCache {

    private static final Cache<String, Resource> cache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    private ResourceCache() {
        // Static class
    }

    public static Resource getOrCache(@Untrusted String resourceName, Supplier<Resource> resourceSupplier) {
        Resource found = cache.getIfPresent(resourceName);
        if (found == null) {
            Resource created = resourceSupplier.get();
            if (created == null) return null;
            return new StringCachingResource(created);
        }
        return found;
    }

    public static void cache(@Untrusted String resourceName, String contents, long lastModifiedDate) {
        cache.put(resourceName, new StringResource(resourceName, contents, lastModifiedDate));
    }

    public static void invalidateAll() {
        cache.invalidateAll();
    }

    public static void cleanUp() {
        cache.cleanUp();
    }

    public static List<String> getCachedResourceNames() {
        List<String> resourceNames = new ArrayList<>(cache.asMap().keySet());
        Collections.sort(resourceNames);
        return resourceNames;
    }
}