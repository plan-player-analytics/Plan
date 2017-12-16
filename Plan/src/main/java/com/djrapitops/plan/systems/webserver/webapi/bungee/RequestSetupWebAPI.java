/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bungee;


import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;
import main.java.com.djrapitops.plan.systems.webserver.response.ForbiddenResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class RequestSetupWebAPI extends WebAPI {

    @Override
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
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
    public void sendRequest(String address) throws WebAPIException {
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