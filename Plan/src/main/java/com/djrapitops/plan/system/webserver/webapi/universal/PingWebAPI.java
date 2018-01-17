/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.universal;


import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import com.djrapitops.plan.systems.info.BukkitInformationManager;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class PingWebAPI extends WebAPI {
    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        if (Check.isBungeeAvailable()) {
            if (!((PlanBungee) plugin).getServerInfoManager().serverConnected(UUID.fromString(variables.get("sender")))) {
                return fail("Server info not found from the database");
            }
        } else if (!plugin.getInfoManager().isUsingAnotherWebServer()) {
            try {
                String webAddress = variables.get("webAddress");
                if (webAddress != null) {
                    ((Plan) plugin).getServerInfoManager().saveBungeeConnectionAddress(webAddress);
                }

                ((BukkitInformationManager) plugin.getInfoManager()).updateConnection();
            } catch (IOException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebException {
        if (Check.isBukkitAvailable()) {
            super.sendRequest(address);
        } else {
            addVariable("webAddress", PlanBungee.getInstance().getWebServer().getAccessAddress());
            super.sendRequest(address);
        }
    }

    public void sendRequest(String address, String accessCode) throws WebException {
        addVariable("accessKey", accessCode);
        addVariable("version", PlanPlugin.getInstance().getVersion());
        sendRequest(address);
    }
}