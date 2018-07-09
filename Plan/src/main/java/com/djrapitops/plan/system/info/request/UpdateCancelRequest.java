/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.system.update.ShutdownUpdateHook;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;

import java.util.Map;

/**
 * InfoRequest used for Updating the plugin on a network.
 *
 * @author Rsl1122
 */
public class UpdateCancelRequest implements InfoRequest {

    public UpdateCancelRequest() {
    }

    public static UpdateCancelRequest createHandler() {
        return new UpdateCancelRequest();
    }

    @Override
    public void runLocally() {
        ShutdownUpdateHook.deActivate();
    }

    @Override
    public Response handleRequest(Map<String, String> variables) {
        ShutdownUpdateHook.deActivate();
        return DefaultResponses.SUCCESS.get();
    }
}
