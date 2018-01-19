/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.connection.NotFoundException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.connection.ConnectionIn;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.NullCheck;

import java.util.List;

/**
 * PageHandler for /info/requestname pages.
 * <p>
 * Used for answering info requests by other servers.
 *
 * @author Rsl1122
 */
public class InfoRequestPageHandler extends PageHandler {

    @Override
    public Response getResponse(Request request, List<String> target) throws WebException {
        if (target.isEmpty()) {
            return DefaultResponses.NOT_FOUND.get();
        }

        String requestName = target.get(0);
        InfoRequest infoRequest = ConnectionSystem.getInstance().getInfoRequest(requestName);

        NullCheck.check(infoRequest, new NotFoundException("Info Request has not been registered."));

        return new ConnectionIn(request, infoRequest).handleRequest();
    }
}