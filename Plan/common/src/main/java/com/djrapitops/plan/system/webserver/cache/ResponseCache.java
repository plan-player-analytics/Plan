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
package com.djrapitops.plan.system.webserver.cache;

import com.djrapitops.plan.system.webserver.response.Response;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * This class contains the page cache.
 * <p>
 * It caches all Responses with their matching identifiers.
 * This reduces CPU cycles and the time to wait for loading the pages.
 * This is especially useful in situations where multiple clients are accessing the server.
 *
 * @author Fuzzlemann
 */
public class ResponseCache {

    private static final Cache<String, Response> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * Constructor used to hide the public constructor
     */
    private ResponseCache() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Loads the response from the response cache.
     * <p>
     * If the {@link Response} isn't cached, {@link Supplier#get()} in the {@code loader}
     * is called to create the Response.
     * <p>
     * If the Response is created, it's automatically cached.
     *
     * @param identifier The identifier of the page
     * @param loader     The The {@link Response} {@link Supplier} (How should it load the page if it's not cached)
     * @return The Response that was cached or created by the the {@link Response} {@link Supplier}
     */
    public static Response loadResponse(String identifier, Supplier<Response> loader) {
        return cache.get(identifier, k -> loader.get());
    }

    /**
     * Loads the page from the page cache.
     *
     * @param identifier The identifier of the page
     * @return The Response that was cached or {@code null} if it wasn't
     */
    public static Response loadResponse(String identifier) {
        return cache.getIfPresent(identifier);
    }

    /**
     * Puts the page into the page cache.
     * <p>
     * If the cache already inherits that {@code identifier}, it's renewed.
     *
     * @param identifier The identifier of the page
     * @param loader     The {@link Response} {@link Supplier} (How it should load the page)
     */
    public static void cacheResponse(String identifier, Supplier<Response> loader) {
        Response response = loader.get();
        cache.put(identifier, response);
    }

    /**
     * Checks if the page is cached.
     *
     * @param identifier The identifier of the page
     * @return true if the page is cached
     */
    public static boolean isCached(String identifier) {
        return cache.getIfPresent(identifier) != null;
    }

    /**
     * Clears the cache from all its contents.
     */
    public static void clearCache() {
        cache.invalidateAll();
    }

    public static Set<String> getCacheKeys() {
        return cache.asMap().keySet();
    }

    public static long getEstimatedSize() {
        return cache.estimatedSize();
    }

    public static void clearResponse(String identifier) {
        cache.invalidate(identifier);
    }
}
