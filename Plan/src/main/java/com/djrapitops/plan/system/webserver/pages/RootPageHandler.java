/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.RedirectResponse;
import com.djrapitops.plan.system.webserver.response.Response;

import java.util.List;
import java.util.Optional;

/**
 * PageHandler for / page (Address root).
 * <p>
 * Not Available if Authentication is not enabled.
 *
 * @author Rsl1122
 */
public class RootPageHandler implements PageHandler {

    @Override
    public Response getResponse(Request request, List<String> target) throws WebException {
        Optional<Authentication> auth = request.getAuth();
        if (!auth.isPresent()) {
            return DefaultResponses.BASIC_AUTH.get();
        }

        WebUser webUser = auth.get().getWebUser();

        int permLevel = webUser.getPermLevel();
        switch (permLevel) {
            case 0:
                return new RedirectResponse("/server");
            case 1:
                return new RedirectResponse("/players");
            case 2:
                return new RedirectResponse("/player/" + webUser.getName());
            default:
                return DefaultResponses.FORBIDDEN.get();
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, List<String> target) {
        return true;
    }
}
