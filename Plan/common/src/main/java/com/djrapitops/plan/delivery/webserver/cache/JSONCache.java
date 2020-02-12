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
package com.djrapitops.plan.delivery.webserver.cache;

import com.djrapitops.plan.delivery.webserver.pages.json.RootJSONResolver;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.storage.file.ResourceCache;
import com.djrapitops.plugin.task.AbsRunnable;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Cache for any JSON data sent via {@link RootJSONResolver}.
 *
 * @author Rsl1122
 */
public class JSONCache {

    private static final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    private JSONCache() {
        // Static class
    }

    public static Response_old getOrCache(String identifier, Supplier<JSONResponse> jsonResponseSupplier) {
        String found = cache.getIfPresent(identifier);
        if (found == null) {
            JSONResponse response = jsonResponseSupplier.get();
            cache.put(identifier, response.getContent());
            return response;
        }
        return new JSONResponse(found);
    }

    public static String getOrCacheString(DataID dataID, UUID serverUUID, Supplier<String> stringSupplier) {
        String identifier = dataID.of(serverUUID);
        String found = cache.getIfPresent(identifier);
        if (found == null) {
            String result = stringSupplier.get();
            cache.put(identifier, result);
            return result;
        }
        return found;
    }

    public static Response_old getOrCache(DataID dataID, Supplier<JSONResponse> jsonResponseSupplier) {
        return getOrCache(dataID.name(), jsonResponseSupplier);
    }

    public static Response_old getOrCache(DataID dataID, UUID serverUUID, Supplier<JSONResponse> jsonResponseSupplier) {
        return getOrCache(dataID.of(serverUUID), jsonResponseSupplier);
    }

    public static void invalidate(String identifier) {
        cache.invalidate(identifier);
    }

    public static void invalidate(DataID dataID) {
        invalidate(dataID.name());
    }

    public static void invalidate(UUID serverUUID, DataID... dataIDs) {
        for (DataID dataID : dataIDs) {
            invalidate(dataID.of(serverUUID));
        }
    }

    public static void invalidate(DataID dataID, UUID serverUUID) {
        invalidate(dataID.of(serverUUID));
    }

    public static void invalidateMatching(DataID... dataIDs) {
        Set<String> toInvalidate = Arrays.stream(dataIDs)
                .map(DataID::name)
                .collect(Collectors.toSet());
        for (String identifier : cache.asMap().keySet()) {
            for (String identifierToInvalidate : toInvalidate) {
                if (StringUtils.startsWith(identifier, identifierToInvalidate)) {
                    invalidate(identifier);
                }
            }
        }
    }

    public static void invalidateMatching(DataID dataID) {
        String toInvalidate = dataID.name();
        for (String identifier : cache.asMap().keySet()) {
            if (StringUtils.startsWith(identifier, toInvalidate)) {
                invalidate(identifier);
            }
        }
    }

    public static void invalidateAll() {
        cache.invalidateAll();
    }

    public static void cleanUp() {
        cache.cleanUp();
    }

    public static List<String> getCachedIDs() {
        List<String> identifiers = new ArrayList<>(cache.asMap().keySet());
        Collections.sort(identifiers);
        return identifiers;
    }

    @Singleton
    public static class CleanTask extends AbsRunnable {

        @Inject
        public CleanTask() {
            // Dagger requires inject constructor
        }

        @Override
        public void run() {
            cleanUp();
            ResourceCache.cleanUp();
        }
    }
}