package com.djrapitops.plan.system.webserver.response.cache;

import com.djrapitops.plan.system.webserver.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class contains the page cache.
 * <p>
 * It caches all Responses with their matching identifiers.
 * This reduces CPU cycles and the time to wait for loading the pages.
 * This is especially useful in situations where multiple clients are accessing the server.
 *
 * @author Fuzzlemann
 * @since 3.6.0
 */
public class ResponseCache {

    private static final Map<String, Response> cache = new HashMap<>();

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
        Response response = loadResponse(identifier);

        if (response != null) {
            return response;
        }

        response = loader.get();

        cache.put(identifier, response);

        return response;
    }

    /**
     * Loads the page from the page cache.
     *
     * @param identifier The identifier of the page
     * @return The Response that was cached or {@code null} if it wasn't
     */
    public static Response loadResponse(String identifier) {
        return cache.get(identifier);
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
        return cache.containsKey(identifier);
    }

    /**
     * Clears the cache from all its contents.
     */
    public static void clearCache() {
        cache.clear();
    }

}
