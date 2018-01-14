/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.Response;

import java.util.List;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class PageHandler {

    /**
     * Get the Response of a PageHandler.
     *
     * @param request Request in case it is useful for choosing page.
     * @param target  Rest of the target coordinates after this page has been solved.
     * @return Response appropriate to the PageHandler.
     */
    public abstract Response getResponse(Request request, List<String> target);

    public boolean isAuthorized(Authentication auth, List<String> target) throws WebUserAuthException {
        return true;
    }

}