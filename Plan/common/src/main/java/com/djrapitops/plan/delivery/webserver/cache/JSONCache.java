package com.djrapitops.plan.delivery.webserver.cache;

import com.djrapitops.plan.delivery.webserver.response.Response;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Cache for any JSON data sent via {@link com.djrapitops.plan.delivery.webserver.pages.json.RootJSONHandler}.
 *
 * @author Rsl1122
 */
@Singleton
public class JSONCache {

    private final Cache<String, String> cache;

    @Inject
    public JSONCache() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .build();
    }

    public Response getOrCache(String identifier, Supplier<JSONResponse> jsonResponseSupplier) {
        String found = cache.getIfPresent(identifier);
        if (found == null) {
            JSONResponse response = jsonResponseSupplier.get();
            cache.put(identifier, response.getContent());
            return response;
        }
        return new JSONResponse(found);
    }

    public Response getOrCache(DataID dataID, Supplier<JSONResponse> jsonResponseSupplier) {
        return getOrCache(dataID.name(), jsonResponseSupplier);
    }
    public Response getOrCache(DataID dataID, UUID serverUUID, Supplier<JSONResponse> jsonResponseSupplier) {
        return getOrCache(dataID.of(serverUUID), jsonResponseSupplier);
    }

    public void invalidate(String identifier) {
        cache.invalidate(identifier);
    }

    public void invalidate(DataID dataID, UUID serverUUID) {
        cache.invalidate(dataID.of(serverUUID));
    }

    public void invalidateMatching(DataID dataID) {
        String toInvalidate = dataID.name();
        for (String identifier : cache.asMap().keySet()) {
            if (StringUtils.startsWith(identifier, toInvalidate)) {
                invalidate(identifier);
            }
        }
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public void cleanUp() {
        cache.cleanUp();
    }
}