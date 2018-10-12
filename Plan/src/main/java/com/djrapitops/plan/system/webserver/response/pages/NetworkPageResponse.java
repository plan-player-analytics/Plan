package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.utilities.html.pages.NetworkPage;

/**
 * Response for /network page.
 *
 * @author Rsl1122
 */
public class NetworkPageResponse extends PageResponse {

    public NetworkPageResponse(NetworkPage networkPage) throws ParseException {
        setHeader("HTTP/1.1 200 OK");
        setContent(networkPage.toHtml());
    }
}