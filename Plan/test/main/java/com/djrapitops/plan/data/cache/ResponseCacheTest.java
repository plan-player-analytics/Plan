package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageLoader;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import org.junit.Test;
import test.utilities.RandomData;

import static junit.framework.TestCase.*;

/**
 * @author Fuzzlemann
 */
public class ResponseCacheTest {
    private final String IDENTIFIER = RandomData.randomString(10);
    private final String RESPONSE_STRING = RandomData.randomString(10);
    private final Response RESPONSE = new Response() {
        @Override
        public String getResponse() {
            return RESPONSE_STRING;
        }
    };
    private final PageLoader LOADER = () -> RESPONSE;

    @Test
    public void testCreateResponse() {
        String expResponse = RESPONSE.getResponse();
        String response = LOADER.createResponse().getResponse();

        assertEquals(expResponse, response);
    }

    @Test
    public void testCache() {
        Response expResponse = LOADER.createResponse();

        assertFalse(ResponseCache.isCached(IDENTIFIER));

        Response response = ResponseCache.loadResponse(IDENTIFIER, LOADER);
        assertTrue(ResponseCache.isCached(IDENTIFIER));

        assertEquals(expResponse, response);
    }

    @Test
    public void testClearCache() {
        ResponseCache.cacheResponse(IDENTIFIER, LOADER);
        assertTrue(ResponseCache.isCached(IDENTIFIER));

        ResponseCache.clearCache();
        assertFalse(ResponseCache.isCached(IDENTIFIER));
    }

    @Test
    public void testRemoveIf() {
        ResponseCache.cacheResponse(IDENTIFIER, LOADER);
        assertTrue(ResponseCache.isCached(IDENTIFIER));

        ResponseCache.removeIf(identifier -> identifier.equals(IDENTIFIER));
        assertFalse(ResponseCache.isCached(IDENTIFIER));
    }
}
