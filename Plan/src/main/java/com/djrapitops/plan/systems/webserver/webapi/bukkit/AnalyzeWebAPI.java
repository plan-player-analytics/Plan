/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.webserver.webapi.bukkit;

import com.djrapitops.plan.api.IPlan;
import com.djrapitops.plan.api.exceptions.WebAPIException;
import com.djrapitops.plan.systems.webserver.response.Response;
import com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class AnalyzeWebAPI extends WebAPI {
    @Override
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
        String serverUUIDS = variables.get("serverUUID");
        if (serverUUIDS == null) {
            return badRequest("serverUUID was not present");
        }
        UUID serverUUID = UUID.fromString(serverUUIDS);
        plugin.getInfoManager().refreshAnalysis(serverUUID);
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendRequest(String address, UUID serverUUID) throws WebAPIException {
        addVariable("serverUUID", serverUUID.toString());
        super.sendRequest(address);
    }
}
