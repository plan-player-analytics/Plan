/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.bungee;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.WebAPIException;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import com.djrapitops.plan.systems.info.BungeeInformationManager;

import java.util.Map;
import java.util.UUID;

/**
 * WebAPI for posting Inspect page Plugins tab contents to the Bungee server.
 * <p>
 * Call: Bukkit to Bungee
 * <p>
 * Bad Requests:
 * - Did not include uuid
 *
 * @author Rsl1122
 */
public class PostInspectPluginsTabWebAPI extends WebAPI {
    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        String uuidS = variables.get("uuid");
        if (uuidS == null) {
            return badRequest("uuid not included");
        }

        UUID uuid = UUID.fromString(uuidS);
        UUID serverUUID = UUID.fromString(variables.get("sender"));
        String nav = variables.get("nav");
        if (nav == null) {
            return badRequest("nav not included");
        }
        String html = variables.get("html");
        if (html == null) {
            return badRequest("html not included");
        }
        String[] content = new String[]{nav, html};

        ((BungeeInformationManager) plugin.getInfoManager()).cachePluginsTabContent(serverUUID, uuid, content);

        return success();
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendPluginsTab(String address, UUID uuid, String[] html) throws WebAPIException {
        addVariable("uuid", uuid.toString());
        addVariable("nav", html[0]);
        addVariable("html", html[1]);
        super.sendRequest(address);
    }
}