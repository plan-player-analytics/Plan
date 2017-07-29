package main.java.com.djrapitops.plan.data.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import main.java.com.djrapitops.plan.ui.webserver.response.Response;

/**
 * This class contains the page cache.
 * <p>
 * It caches all Responses with their matching identifiers.
 * This reduces CPU cycles and the time to wait for loading the pages.
 * This is especially useful in situations where multiple clients are accessing the server.
 * <p>
 * This cache uses the Google Guava {@link Cache}.
 *
 * @author Fuzzlemann
 */
public class PageCacheHandler {

    /**
     * Constructor used to hide the public constructor
     */
    private PageCacheHandler() {
        throw new IllegalStateException("Utility class");
    }

    private static final Cache<String, Response> pageCache = CacheBuilder.newBuilder()
            .build();

    /**
     * Loads the page from the page cache.
     * <p>
     * If the {@link Response} isn't cached, {@link PageLoader#createResponse()} in the {@code loader}
     * is called to create the Response.
     * <p>
     * If the Response is created, it's automatically cached.
     *
     * @param identifier
     * @param loader
     * @return
     */
    public static Response loadPage(String identifier, PageLoader loader) {
        Response response = pageCache.asMap().get(identifier);

        if (response != null) {
            return response;
        }

        response = loader.createResponse();

        pageCache.put(identifier, response);

        return response;
    }

    /**
     * Clears the cache from all its contents.
     */
    public static void clearCache() {
        pageCache.asMap().clear();
    }
}
