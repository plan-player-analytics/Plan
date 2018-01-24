/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.bungee;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import com.djrapitops.plugin.api.Check;

import java.util.Map;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
@Deprecated
public class RequestSetupWebAPI extends WebAPI {

    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        return fail("Deprecated");
    }

    @Override
    public void sendRequest(String address) throws WebException {
        if (!Check.isBukkitAvailable()) {
            throw new IllegalStateException("Not supposed to be called on Bungee");
        }

        Plan plugin = Plan.getInstance();
        addVariable("webAddress", plugin.getWebServer().getAccessAddress());
        super.sendRequest(address);
    }
}