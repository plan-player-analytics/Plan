package test.java.main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.data.cache.PageCacheHandler;
import main.java.com.djrapitops.plan.data.cache.PageLoader;
import main.java.com.djrapitops.plan.ui.webserver.response.Response;
import org.junit.Test;
import test.java.utils.RandomData;

import static junit.framework.TestCase.*;

/**
 * @author Fuzzlemann
 */
public class PageCacheHandlerTest {
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

        assertFalse(PageCacheHandler.isCached(IDENTIFIER));

        Response response = PageCacheHandler.loadPage(IDENTIFIER, LOADER);
        assertTrue(PageCacheHandler.isCached(IDENTIFIER));

        assertEquals(expResponse, response);
    }

    @Test
    public void testClearCache() {
        PageCacheHandler.cachePage(IDENTIFIER, LOADER);
        assertTrue(PageCacheHandler.isCached(IDENTIFIER));

        PageCacheHandler.clearCache();
        assertFalse(PageCacheHandler.isCached(IDENTIFIER));
    }

    @Test
    public void testRemoveIf() {
        PageCacheHandler.cachePage(IDENTIFIER, LOADER);
        assertTrue(PageCacheHandler.isCached(IDENTIFIER));

        PageCacheHandler.removeIf(identifier -> identifier.equals(IDENTIFIER));
        assertFalse(PageCacheHandler.isCached(IDENTIFIER));
    }
}
