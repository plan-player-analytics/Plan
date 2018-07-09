/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.update.ShutdownUpdateHook;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.api.BadRequestResponse;

import java.util.Map;

/**
 * InfoRequest used for Updating the plugin on a network.
 *
 * @author Rsl1122
 */
public class UpdateRequest implements InfoRequest {

    public UpdateRequest() {
    }

    public static UpdateRequest createHandler() {
        return new UpdateRequest();
    }

    @Override
    public void runLocally() {
        new ShutdownUpdateHook().register();
    }

    @Override
    public Response handleRequest(Map<String, String> variables) {
        if (Settings.ALLOW_UPDATE.isTrue()) {
            new ShutdownUpdateHook().register();
            return DefaultResponses.SUCCESS.get();
        } else {
            return new BadRequestResponse("Update not allowed on this server");
        }
    }
}
