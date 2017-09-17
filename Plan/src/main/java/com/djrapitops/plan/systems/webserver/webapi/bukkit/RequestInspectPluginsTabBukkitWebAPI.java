/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit;

import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class RequestInspectPluginsTabBukkitWebAPI extends WebAPI {

    @Override
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
        if (!Compatibility.isBukkitAvailable()) {
            return badRequest("Called a Bungee Server");
        }

        String uuidS = variables.get("uuid");
        if (uuidS == null) {
            return badRequest("UUID not included");
        }
        UUID uuid = UUID.fromString(uuidS);

        ((BukkitInformationManager) plugin.getInfoManager()).cacheInspectPluginsTab(uuid, this.getClass());
        return PageCache.loadPage("success", SuccessResponse::new);
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendRequest(String address, UUID uuid) throws WebAPIException {
        addVariable("uuid", uuid.toString());
        super.sendRequest(address);
    }
}