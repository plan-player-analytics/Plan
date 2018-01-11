package com.djrapitops.plan.systems.webserver.pagecache;

import com.djrapitops.plan.systems.webserver.response.Response;

/**
 * This interface is used for providing the method to load the page.
 *
 * @author Fuzzlemann
 * @since 3.6.0
 */
public interface PageLoader {

    Response createResponse();

}
