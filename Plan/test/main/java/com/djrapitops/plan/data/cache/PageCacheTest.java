package main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.PageLoader;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import org.junit.Test;
import test.java.utils.RandomData;

import static junit.framework.TestCase.*;

/**
 * @author Fuzzlemann
 */
public class PageCacheTest {
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

        assertFalse(PageCache.isCached(IDENTIFIER));

        Response response = PageCache.loadPage(IDENTIFIER, LOADER);
        assertTrue(PageCache.isCached(IDENTIFIER));

        assertEquals(expResponse, response);
    }

    @Test
    public void testClearCache() {
        PageCache.cachePage(IDENTIFIER, LOADER);
        assertTrue(PageCache.isCached(IDENTIFIER));

        PageCache.clearCache();
        assertFalse(PageCache.isCached(IDENTIFIER));
    }

    @Test
    public void testRemoveIf() {
        PageCache.cachePage(IDENTIFIER, LOADER);
        assertTrue(PageCache.isCached(IDENTIFIER));

        PageCache.removeIf(identifier -> identifier.equals(IDENTIFIER));
        assertFalse(PageCache.isCached(IDENTIFIER));
    }
}
