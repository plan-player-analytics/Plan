package main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.ui.webserver.response.Response;

/**
 * This interface is used for providing the method to load the page.
 *
 * @author Fuzzlemann
 * @since 3.6.0
 */
public interface PageLoader {

    Response createResponse();

}
