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

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.webserver.resolver.json.RootJSONResolver;
import com.djrapitops.plan.storage.file.ResourceCache;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Cache for any JSON data sent via {@link RootJSONResolver}.
 *
 * @author Rsl1122
 */
@Deprecated
public class JSONCache {

    private static final Cache<String, byte[]> cache = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    private JSONCache() {
        // Static class
    }

    @Deprecated
    public static Response getOrCache(String identifier, Supplier<Response> jsonResponseSupplier) {
        byte[] found = cache.getIfPresent(identifier);
        if (found == null) {
            Response response = jsonResponseSupplier.get();
            cache.put(identifier, response.getBytes());
            return response;
        }
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setContent(found)
                .build();
    }

    @Deprecated
    public static String getOrCacheString(DataID dataID, UUID serverUUID, Supplier<String> stringSupplier) {
        String identifier = dataID.of(serverUUID);
        byte[] found = cache.getIfPresent(identifier);
        if (found == null) {
            String result = stringSupplier.get();
            cache.put(identifier, result.getBytes(StandardCharsets.UTF_8));
            return result;
        }
        return new String(found, StandardCharsets.UTF_8);
    }

    @Deprecated
    public static <T> Response getOrCache(DataID dataID, Supplier<T> objectSupplier) {
        return getOrCache(dataID.name(), () -> Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(objectSupplier.get())
                .build());
    }

    @Deprecated
    public static <T> Response getOrCache(DataID dataID, UUID serverUUID, Supplier<T> objectSupplier) {
        return getOrCache(dataID.of(serverUUID), () -> Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(objectSupplier.get())
                .build());
    }

    @Deprecated
    public static void invalidate(String identifier) {
        cache.invalidate(identifier);
    }

    @Deprecated
    public static void invalidate(DataID dataID) {
        invalidate(dataID.name());
    }

    @Deprecated
    public static void invalidate(UUID serverUUID, DataID... dataIDs) {
        for (DataID dataID : dataIDs) {
            invalidate(dataID.of(serverUUID));
        }
    }

    @Deprecated
    public static void invalidate(DataID dataID, UUID serverUUID) {
        invalidate(dataID.of(serverUUID));
    }

    @Deprecated
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

    @Deprecated
    public static void invalidateMatching(DataID dataID) {
        String toInvalidate = dataID.name();
        for (String identifier : cache.asMap().keySet()) {
            if (StringUtils.startsWith(identifier, toInvalidate)) {
                invalidate(identifier);
            }
        }
    }

    @Deprecated
    public static void invalidateAll() {
        cache.invalidateAll();
    }

    @Deprecated
    public static void cleanUp() {
        cache.cleanUp();
    }

    @Deprecated
    public static List<String> getCachedIDs() {
        List<String> identifiers = new ArrayList<>(cache.asMap().keySet());
        Collections.sort(identifiers);
        return identifiers;
    }

    @Singleton
    public static class CleanTask extends TaskSystem.Task {

        @Inject
        public CleanTask() {
            // Dagger requires inject constructor
        }

        @Override
        public void run() {
            cleanUp();
            ResourceCache.cleanUp();
        }

        @Override
        public void register(RunnableFactory runnableFactory) {
            long minute = TimeAmount.toTicks(1, TimeUnit.MINUTES);
            runnableFactory.create(null, this).runTaskTimerAsynchronously(minute, minute);
        }
    }
}