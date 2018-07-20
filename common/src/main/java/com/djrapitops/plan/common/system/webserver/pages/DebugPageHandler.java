/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.common.system.webserver.pages;

import com.djrapitops.plan.common.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.common.data.WebUser;
import com.djrapitops.plan.common.system.webserver.Request;
import com.djrapitops.plan.common.system.webserver.auth.Authentication;
import com.djrapitops.plan.common.system.webserver.response.Response;
import com.djrapitops.plan.common.system.webserver.response.pages.DebugPageResponse;

import java.util.List;

/**
 * PageHandler for /debug page.
 *
 * @author Rsl1122
 */
public class DebugPageHandler extends PageHandler {

    @Override
    public Response getResponse(Request request, List<String> target) {
        return new DebugPageResponse();
    }

    @Override
    public boolean isAuthorized(Authentication auth, List<String> target) throws WebUserAuthException {
        WebUser webUser = auth.getWebUser();
        return webUser.getPermLevel() <= 0;
    }
}
