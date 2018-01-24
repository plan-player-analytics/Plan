/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.bungee;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import com.djrapitops.plugin.api.Check;

import java.util.Map;
import java.util.UUID;

/**
 * WebAPI for requesting Bungee Server to request Plugins tab contents from every server.
 * <p>
 * Call: Bukkit to Bungee
 * <p>
 * Bad Requests:
 * - Called a Bukkit Server
 * - Did not include uuid variable
 *
 * @author Rsl1122
 */
public class RequestPluginsTabWebAPI extends WebAPI {
    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        if (!Check.isBungeeAvailable()) {
            return badRequest("Called a Bukkit Server");
        }

        String uuidS = variables.get("uuid");
        if (uuidS == null) {
            return badRequest("UUID not included");
        }
        UUID uuid = UUID.fromString(uuidS);

        sendRequestsToBukkitServers(plugin, uuid);
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendRequest(String address, UUID uuid) throws WebException {
        addVariable("uuid", uuid.toString());
        super.sendRequest(address);
    }

    public void sendRequestsToBukkitServers(PlanPlugin plugin, UUID uuid) {
    }
}