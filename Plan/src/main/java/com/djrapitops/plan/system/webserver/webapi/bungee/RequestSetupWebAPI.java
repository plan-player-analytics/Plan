/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.bungee;


import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.ForbiddenResponse;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import com.djrapitops.plan.systems.info.server.ServerInfo;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class RequestSetupWebAPI extends WebAPI {

    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        if (!Check.isBungeeAvailable()) {
            return badRequest("Called a Bukkit server.");
        }

        if (!((PlanBungee) plugin).isSetupAllowed()) {
            return new ForbiddenResponse("Setup mode disabled, use /planbungee setup to enable");
        }

        String serverUUIDS = variables.get("sender");
        String webAddress = variables.get("webAddress");
        String accessCode = variables.get("accessKey");
        if (!Verify.notNull(serverUUIDS, webAddress, accessCode)) {
            return badRequest("Variable was null");
        }
        ServerInfo serverInfo = new ServerInfo(-1, UUID.fromString(serverUUIDS), "", webAddress, 0);

        ((PlanBungee) plugin).getServerInfoManager().attemptConnection(serverInfo, accessCode);
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebException {
        if (!Check.isBukkitAvailable()) {
            throw new IllegalStateException("Not supposed to be called on Bungee");
        }

        Plan plugin = Plan.getInstance();
        try {
            addVariable("accessKey", plugin.getWebServer().getWebAPI().generateNewAccessKey());
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
        }
        addVariable("webAddress", plugin.getWebServer().getAccessAddress());
        super.sendRequest(address);
    }
}