/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * @author Fuzzlemann
 */
public class InspectWebAPI extends WebAPI {
    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        String uuidS = variables.get("uuid");
        if (uuidS == null) {
            String error = "UUID not included";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }
        UUID uuid = UUID.fromString(uuidS);

        plugin.getInfoManager().cachePlayer(uuid);

        return PageCache.loadPage("success", SuccessResponse::new);
    }

    public void sendRequest(String address, UUID receiverUUID, UUID uuid) throws WebAPIException {
        addVariable("uuid", uuid.toString());
        super.sendRequest(address, receiverUUID);
    }
}
