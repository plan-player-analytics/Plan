package test.java.main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.data.cache.PageCacheHandler;
import main.java.com.djrapitops.plan.data.cache.PageLoader;
import main.java.com.djrapitops.plan.ui.webserver.response.Response;
import org.junit.Test;

import static junit.framework.TestCase.*;

/**
 * @author Fuzzlemann
 */
public class PageCacheHandlerTest {
    private final String IDENTIFIER = "test";
    private final PageLoader LOADER = () -> new Response() {
        @Override
        public String getResponse() {
            return "Test";
        }
    };

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
}
