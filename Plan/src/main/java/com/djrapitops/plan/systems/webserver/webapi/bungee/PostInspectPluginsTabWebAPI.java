/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bungee;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.info.BungeeInformationManager;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

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
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
        String uuidS = variables.get("uuid");
        if (uuidS == null) {
            return badRequest("uuid not included");
        }

        UUID uuid = UUID.fromString(uuidS);
        UUID serverUUID = UUID.fromString(variables.get("sender"));
        String html = variables.get("html");

        ((BungeeInformationManager) plugin.getInfoManager()).cachePluginsTabContent(serverUUID, uuid, html);

        return success();
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendPluginsTab(String address, UUID uuid, String html) throws WebAPIException {
        addVariable("uuid", uuid.toString());
        addVariable("html", html);
        super.sendRequest(address);
    }
}