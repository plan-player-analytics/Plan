/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;

import java.util.Map;

/**
 * InfoRequest for generating network page content of a Bukkit server.
 *
 * @author Rsl1122
 */
public class GenerateNetworkPageContentRequest implements WideRequest, GenerateRequest {

    private final InfoSystem infoSystem;

    GenerateNetworkPageContentRequest(InfoSystem infoSystem) {
        this.infoSystem = infoSystem;
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        runLocally();
        return DefaultResponses.SUCCESS.get();
    }

    @Override
    public void runLocally() throws WebException {
        infoSystem.updateNetworkPage();
    }
}
