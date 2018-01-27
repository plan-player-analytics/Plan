package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import org.junit.Test;
import utilities.RandomData;

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

    @Test
    public void testCache() {
        assertFalse(ResponseCache.isCached(IDENTIFIER));

        Response response = ResponseCache.loadResponse(IDENTIFIER, () -> RESPONSE);
        assertTrue(ResponseCache.isCached(IDENTIFIER));

        assertEquals(RESPONSE, response);
    }

    @Test
    public void testClearCache() {
        ResponseCache.cacheResponse(IDENTIFIER, () -> RESPONSE);
        assertTrue(ResponseCache.isCached(IDENTIFIER));

        ResponseCache.clearCache();
        assertFalse(ResponseCache.isCached(IDENTIFIER));
    }

    @Test
    public void testRemoveIf() {
        ResponseCache.cacheResponse(IDENTIFIER, () -> RESPONSE);
        assertTrue(ResponseCache.isCached(IDENTIFIER));

        ResponseCache.removeIf(identifier -> identifier.equals(IDENTIFIER));
        assertFalse(ResponseCache.isCached(IDENTIFIER));
    }
}
